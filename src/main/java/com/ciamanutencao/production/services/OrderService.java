package com.ciamanutencao.production.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ciamanutencao.production.dto.OrderDTO;
import com.ciamanutencao.production.dto.OrderItemDTO;
import com.ciamanutencao.production.entities.Category;
import com.ciamanutencao.production.entities.Order;
import com.ciamanutencao.production.entities.OrderItem;
import com.ciamanutencao.production.entities.Technical;
import com.ciamanutencao.production.entities.User;
import com.ciamanutencao.production.enums.OrderStatus;
import com.ciamanutencao.production.enums.UserRole;
import com.ciamanutencao.production.exceptions.ResourceNotFoundException;
import com.ciamanutencao.production.repositories.CategoryRepository;
import com.ciamanutencao.production.repositories.OrderRepository;
import com.ciamanutencao.production.repositories.TechnicalRepository;
import com.ciamanutencao.production.repositories.UserRepository;
import com.ciamanutencao.production.services.utils.SecurityUtils;

import jakarta.persistence.EntityNotFoundException;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TechnicalRepository technicalRepository;
    private final SecurityUtils securityUtils;

    public OrderService(OrderRepository orderRepository, CategoryRepository categoryRepository,
            UserRepository userRepository, TechnicalRepository technicalRepository,
            SecurityUtils securityUtils) {
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.technicalRepository = technicalRepository;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public OrderDTO createOrder(OrderDTO dto) {
        User requester = userRepository.findById(dto.requester().id())
                .orElseThrow(() -> new ResourceNotFoundException("Requisitante não cadastrado"));

        securityUtils.checkDepartmentAccess(requester.getDepartment());

        Technical technical = technicalRepository.findById(dto.technical().id())
                .orElseThrow(() -> new ResourceNotFoundException("Técnico não cadastrado"));

        securityUtils.checkDepartmentAccess(technical.getDepartments());

        Category category = categoryRepository.findById(dto.category().id())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Categoria não encontrada com ID: " + dto.category().id()));

        Integer currentYear = LocalDate.now().getYear();
        Integer lastSequence = orderRepository.findMaxSequenceByYear(currentYear);
        int nextSequence = (lastSequence == null) ? 1 : lastSequence + 1;
        String formattedNumber = String.format("%04d/%d", nextSequence, currentYear);

        Order order = new Order();
        order.setCategory(category);
        order.setRequester(requester);
        order.setTechnical(technical);
        order.setServiceLocation(dto.serviceLocation());
        order.setOpeningDate(LocalDateTime.now());
        order.setObservation(dto.observation());
        order.setOrderStatus(OrderStatus.OPEN);
        order.setOrderNumber(formattedNumber);
        order.setSequence(nextSequence);
        order.setYear(currentYear);

        order = orderRepository.save(order);
        return new OrderDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> findAllOrders() {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<Order> list;
        if (currentUser.getUserRole() == UserRole.ROLE_ADMIN) {
            list = orderRepository.findAll();
        } else {
            list = orderRepository.findByRequesterDepartmentId(currentUser.getDepartment().getId());
        }
        return list.stream().map(OrderDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public OrderDTO findOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

        securityUtils.checkDepartmentAccess(order.getRequester().getDepartment());

        return new OrderDTO(order);
    }

    @Transactional(readOnly = true)
    public OrderDTO findOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de Serviço não encontrada: " + orderNumber));

        securityUtils.checkDepartmentAccess(order.getRequester().getDepartment());
        return new OrderDTO(order);
    }

    @Transactional
    public OrderDTO updateOrder(Long id, OrderDTO updatedOrder) {
        try {
            Order entity = orderRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(id));

            securityUtils.checkDepartmentAccess(entity.getRequester().getDepartment());

            Category category = categoryRepository.findById(updatedOrder.category().id())
                    .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada"));

            Technical technical = technicalRepository.findById(updatedOrder.technical().id())
                    .orElseThrow(() -> new ResourceNotFoundException("Técnico não encontrado"));

            entity.setCategory(category);
            entity.setTechnical(technical);
            entity.setServiceLocation(updatedOrder.serviceLocation());
            entity.setObservation(updatedOrder.observation());
            entity.setOrderStatus(updatedOrder.orderStatus());

            return new OrderDTO(orderRepository.save(entity));
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFoundException(id);
        }
    }

    @Transactional
    public OrderDTO finalizeOrder(String orderNumber, LocalDateTime closingDate) {
        Order orderToFinalize = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("OS não encontrada: " + orderNumber));

        securityUtils.checkDepartmentAccess(orderToFinalize.getRequester().getDepartment());

        orderToFinalize.setClosingDate(closingDate);
        orderToFinalize.setOrderStatus(OrderStatus.FINISHED);

        return new OrderDTO(orderRepository.save(orderToFinalize));
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order entity = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        securityUtils.checkDepartmentAccess(entity.getRequester().getDepartment());

        orderRepository.delete(entity);
    }

    @Transactional
    public OrderDTO addItemsFromOrder(Long orderId, List<OrderItemDTO> itemDTOs) {
        Order orderToAddItems = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de Serviço não encontrada"));

        securityUtils.checkDepartmentAccess(orderToAddItems.getRequester().getDepartment());

        for (OrderItemDTO item : itemDTOs) {
            OrderItem itemEntity = new OrderItem();
            itemEntity.setItemDescription(item.itemDescription());
            itemEntity.setItemPrice(item.itemPrice());
            itemEntity.setSerialNumber(item.serialNumber());
            itemEntity.setServiceDescription(item.serviceDescription());
            itemEntity.setServicePrice(item.servicePrice());
            itemEntity.setItemNumber(orderToAddItems.getItems().size() + 1);

            orderToAddItems.addItem(itemEntity);
        }

        return new OrderDTO(orderRepository.save(orderToAddItems));
    }

    @Transactional
    public OrderDTO removeItemsFromOrder(Long orderId, List<Integer> itemsNumbers) {
        Order orderToRemoveItems = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordem de Serviço não encontrada"));

        securityUtils.checkDepartmentAccess(orderToRemoveItems.getRequester().getDepartment());

        boolean removedItems = orderToRemoveItems.getItems()
                .removeIf(item -> itemsNumbers.contains(item.getItemNumber()));

        if (!removedItems) {
            throw new ResourceNotFoundException("Nenhum dos itens informados foi encontrado.");
        }

        for (int i = 0; i < orderToRemoveItems.getItems().size(); i++) {
            orderToRemoveItems.getItems().get(i).setItemNumber(i + 1);
        }

        return new OrderDTO(orderRepository.save(orderToRemoveItems));
    }
}