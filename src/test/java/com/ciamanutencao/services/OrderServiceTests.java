package com.ciamanutencao.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ciamanutencao.production.dto.CategoryDTO;
import com.ciamanutencao.production.dto.OrderDTO;
import com.ciamanutencao.production.dto.OrderItemDTO;
import com.ciamanutencao.production.dto.TechnicalDTO;
import com.ciamanutencao.production.dto.UserDTO;
import com.ciamanutencao.production.entities.Category;
import com.ciamanutencao.production.entities.Department;
import com.ciamanutencao.production.entities.Order;
import com.ciamanutencao.production.entities.OrderItem;
import com.ciamanutencao.production.entities.Technical;
import com.ciamanutencao.production.entities.User;
import com.ciamanutencao.production.enums.OrderStatus;
import com.ciamanutencao.production.enums.UserRole;
import com.ciamanutencao.production.exceptions.ResourceNotFoundException;
import com.ciamanutencao.production.repositories.CategoryRepository;
import com.ciamanutencao.production.repositories.DepartmentRepository;
import com.ciamanutencao.production.repositories.OrderRepository;
import com.ciamanutencao.production.repositories.TechnicalRepository;
import com.ciamanutencao.production.repositories.UserRepository;
import com.ciamanutencao.production.services.OrderService;
import com.ciamanutencao.production.services.utils.SecurityUtils;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTests {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TechnicalRepository technicalRepository;

    private Long existingId;
    private Long nonExistingId;
    private User userTest;
    private Order orderTest;
    private OrderDTO orderDTOTest;
    private Department departmentTest;
    private Technical technicalTest;
    private Category categoryTest;

    @BeforeEach
    void setUp() {
        existingId = 1L;
        nonExistingId = 100L;

        departmentTest = new Department(1L, "Dep Test", true);
        categoryTest = new Category(existingId, "Cat Test", true);
        technicalTest = new Technical(existingId, "Tech Test", true);
        userTest = new User(existingId, departmentTest, "User test", "login", "pass", UserRole.ROLE_ADMIN, true);

        orderTest = new Order(existingId, categoryTest, userTest, technicalTest,
                OrderStatus.ON_HOLD, "10/2026", 10, 2026,
                LocalDateTime.now(), null, "27º Batalhão Logístico", "Observation test");

        CategoryDTO catDTO = new CategoryDTO(categoryTest);
        UserDTO userDTO = new UserDTO(userTest);
        TechnicalDTO techDTO = new TechnicalDTO(technicalTest);

        orderDTOTest = new OrderDTO(null, "10/2026", 10, 2026, catDTO, userDTO, techDTO,
                OrderStatus.ON_HOLD, LocalDateTime.now(), null, "27º Batalhão Logístico", "Observation test", List.of(),
                BigDecimal.ZERO);
    }

    @Test
    @DisplayName("createOrder deve retornar OrderDTO quando os dados forem válidos")
    void createOrderMustReturnOrderDTO() {

        when(userRepository.findById(any())).thenReturn(Optional.of(userTest));
        when(technicalRepository.findById(any())).thenReturn(Optional.of(technicalTest));
        when(categoryRepository.findById(any())).thenReturn(Optional.of(categoryTest));

        when(orderRepository.findMaxSequenceByYear(anyInt())).thenReturn(10);

        when(orderRepository.save(any(Order.class))).thenReturn(orderTest);

        doNothing().when(securityUtils).checkDepartmentAccess(departmentTest);

        OrderDTO result = orderService.createOrder(orderDTOTest);

        assertNotNull(result);
        assertEquals("10/2026", result.orderNumber());
        assertEquals(OrderStatus.ON_HOLD, result.orderStatus());

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("findAllOrders deve retornar todas as ordens quando o usuário é ADMIN")
    void findAllOrdersMustReturnAllOrdersWhenUserIsAdmin() {

        try (MockedStatic<SecurityContextHolder> mockedSecurity = mockStatic(SecurityContextHolder.class)) {
            SecurityContext mockContext = mock(SecurityContext.class);
            Authentication mockAuth = mock(Authentication.class);

            mockedSecurity.when(SecurityContextHolder::getContext).thenReturn(mockContext);
            when(mockContext.getAuthentication()).thenReturn(mockAuth);
            when(mockAuth.getPrincipal()).thenReturn(userTest);
            when(orderRepository.findAll()).thenReturn(List.of(orderTest));

            List<OrderDTO> result = orderService.findAllOrders();

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(orderTest.getOrderNumber(), result.get(0).orderNumber());
            verify(orderRepository, times(1)).findAll();
            verify(orderRepository, never()).findByRequesterDepartmentId(userTest.getDepartment().getId());

        }
    }

    @Test
    @DisplayName("findAllOrders deve retornar apenas ordens do departamento quando o usuário é CHIEF")
    void findAllOrdersMustReturnAllOrdersFromDepartmentWhenUserIsChief() {

        userTest.setUserRole(UserRole.ROLE_CHIEF);

        try (MockedStatic<SecurityContextHolder> mockSecurity = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication mockAuth = mock(Authentication.class);

            mockSecurity.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(mockAuth);
            when(mockAuth.getPrincipal()).thenReturn(userTest);
            when(orderRepository.findByRequesterDepartmentId(userTest.getDepartment().getId()))
                    .thenReturn(List.of(orderTest));

            List<OrderDTO> result = orderService.findAllOrders();

            assertNotNull(result);
            assertFalse(result.isEmpty());
            verify(orderRepository, never()).findAll();
            verify(orderRepository, times(1)).findByRequesterDepartmentId(userTest.getDepartment().getId());

        }
    }

    @Test
    @DisplayName("findOrderById deve retornar OrderDTO quando o ID existe")
    void findOrderByIdShouldReturnOrderDTOWhenIdExists() {

        doNothing().when(securityUtils).checkDepartmentAccess(departmentTest);
        when(orderRepository.findById(existingId)).thenReturn(Optional.of(orderTest));

        OrderDTO result = orderService.findOrderById(existingId);

        assertNotNull(result);
        verify(orderRepository, times(1)).findById(existingId);
    }

    @Test
    @DisplayName("findOrderById deve lançar ResourceNotFoundException quando o ID não existe")
    void findOrderByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        when(orderRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            orderService.findOrderById(nonExistingId);
        }).isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository, times(1)).findById(nonExistingId);
    }

    @Test
    @DisplayName("findOrderByOrderNumber deve retornar OrderDTO quando o número da OS existe")
    void findOrderByOrderNumberShouldReturnOrderDTOWhenOrderNumberExists() {

        when(orderRepository.findByOrderNumber("10/2026")).thenReturn(Optional.of(orderTest));
        doNothing().when(securityUtils).checkDepartmentAccess(departmentTest);

        OrderDTO result = orderService.findOrderByOrderNumber("10/2026");

        assertNotNull(result);
        verify(orderRepository, times(1)).findByOrderNumber("10/2026");

    }

    @Test
    @DisplayName("findOrderByOrderNumber deve lançar ResourceNotFoundException quando o número da OS não existe")
    void findOrderByOrderNumberShouldThrowResourceNotFoundExceptionWhenOrderNumberDoesNotExist() {

        when(orderRepository.findByOrderNumber("11/2026")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> {
            orderService.findOrderByOrderNumber("11/2026");
        }).isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository, times(1)).findByOrderNumber("11/2026");
    }

    @Test
    @DisplayName("Deve atualizar uma ordem com sucesso quando dados são válidos e usuário tem acesso")
    void updateOrderMustReturnUpdatedOrderDTOWhenDataIsValid() {

        when(orderRepository.findById(existingId)).thenReturn(Optional.of(orderTest));
        doNothing().when(securityUtils).checkDepartmentAccess(departmentTest);
        when(categoryRepository.findById(categoryTest.getId())).thenReturn(Optional.of(categoryTest));
        when(technicalRepository.findById(technicalTest.getId())).thenReturn(Optional.of(technicalTest));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDTO result = orderService.updateOrder(existingId, orderDTOTest);

        assertNotNull(result);
        verify(orderRepository, times(1)).findById(existingId);
        verify(categoryRepository, times(1)).findById(orderDTOTest.category().id());
        verify(technicalRepository, times(1)).findById(orderDTOTest.technical().id());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(securityUtils, times(1)).checkDepartmentAccess(departmentTest);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o ID da OS não existir")
    void updateOrderShouldThrowExceptionWhenOrderIdDoesNotExist() {

        when(orderRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.updateOrder(nonExistingId, orderDTOTest);
        });

        verify(categoryRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o usuário não tem acesso ao departamento da OS")
    void updateOrderShouldThrowExceptionWhenSecurityCheckFails() {

        when(orderRepository.findById(existingId)).thenReturn(Optional.of(orderTest));

        doThrow(new RuntimeException("Acesso negado"))
                .when(securityUtils).checkDepartmentAccess(departmentTest);

        assertThrows(RuntimeException.class, () -> {
            orderService.updateOrder(existingId, orderDTOTest);
        });

        verify(categoryRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando a Categoria informada não existir")
    void updateOrderShouldThrowExceptionWhenCategoryNotFound() {

        when(orderRepository.findById(existingId)).thenReturn(Optional.of(orderTest));
        doNothing().when(securityUtils).checkDepartmentAccess(departmentTest);
        when(categoryRepository.findById(orderDTOTest.category().id())).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            orderService.updateOrder(existingId, orderDTOTest);
        });

        assertEquals("Categoria não encontrada", exception.getMessage());
        verify(technicalRepository, never()).findById(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve finalizar ordem com sucesso quando dados são válidos")
    void finalizeOrderMustSuccess() {

        String orderNumber = "001/2024";
        LocalDateTime closingDate = LocalDateTime.now();

        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.of(orderTest));
        doNothing().when(securityUtils).checkDepartmentAccess(departmentTest);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        OrderDTO result = orderService.finalizeOrder(orderNumber, closingDate);

        assertNotNull(result);
        assertEquals(OrderStatus.FINISHED, result.orderStatus());
        verify(orderRepository).save(orderTest);
        verify(securityUtils).checkDepartmentAccess(orderTest.getRequester().getDepartment());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o número da OS não existe")
    void finalizeOrderShouldThrowExceptionWhenNotFound() {
        String orderNumber = "999/2024";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> orderService.finalizeOrder(orderNumber, LocalDateTime.now()));

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve deletar ordem com sucesso")
    void deleteOrderMustSuccess() {
        when(orderRepository.findById(existingId)).thenReturn(Optional.of(orderTest));
        doNothing().when(securityUtils).checkDepartmentAccess(departmentTest);

        assertDoesNotThrow(() -> orderService.deleteOrder(existingId));

        verify(orderRepository, times(1)).delete(orderTest);
        verify(securityUtils).checkDepartmentAccess(orderTest.getRequester().getDepartment());
    }

    @Test
    @DisplayName("Não deve deletar e lançar exceção quando acesso é negado")
    void deleteOrderShouldThrowExceptionWhenSecurityFails() {
        when(orderRepository.findById(existingId)).thenReturn(Optional.of(orderTest));

        doThrow(new RuntimeException("Acesso Negado"))
                .when(securityUtils).checkDepartmentAccess(departmentTest);

        assertThrows(RuntimeException.class, () -> orderService.deleteOrder(existingId));

        verify(orderRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando ID não existe")
    void deleteOrderShouldThrowExceptionWhenIdNotFound() {
        when(orderRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.deleteOrder(nonExistingId));

        verify(orderRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve adicionar itens vinculando-os à ordem e calculando número sequencial")
    void addItemsFromOrderMustSuccess() {

        Long orderId = 1L;
        OrderItemDTO itemDto = new OrderItemDTO(
                null, null, "Item Teste", new BigDecimal("100.00"),
                "SN123", "Serviço", new BigDecimal("50.00"), null);

        List<OrderItemDTO> orderItemDTO = List.of(itemDto);

        orderTest.getItems().clear();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(orderTest));
        doNothing().when(securityUtils).checkDepartmentAccess(departmentTest);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        orderService.addItemsFromOrder(orderId, orderItemDTO);

        assertEquals(1, orderTest.getItems().size());
        OrderItem savedItem = orderTest.getItems().get(0);
        assertEquals(orderTest, savedItem.getOrder());
        assertEquals(1, savedItem.getItemNumber());
        verify(orderRepository).save(orderTest);
    }

    @Test
    @DisplayName("Deve remover itens específicos e reordenar sequencialmente os restantes")
    void removeItemsFromOrderMustSuccessAndReorder() {

        OrderItem i1 = new OrderItem();
        i1.setItemNumber(1);
        OrderItem i2 = new OrderItem();
        i2.setItemNumber(2);
        OrderItem i3 = new OrderItem();
        i3.setItemNumber(3);

        orderTest.getItems().clear();
        orderTest.addItem(i1);
        orderTest.addItem(i2);
        orderTest.addItem(i3);

        when(orderRepository.findById(existingId)).thenReturn(Optional.of(orderTest));
        doNothing().when(securityUtils).checkDepartmentAccess(departmentTest);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        List<Integer> toRemove = List.of(1, 2);

        orderService.removeItemsFromOrder(existingId, toRemove);

        assertEquals(1, orderTest.getItems().size());
        assertEquals(1, orderTest.getItems().get(0).getItemNumber());
        verify(orderRepository).save(orderTest);
    }

    @Test
    @DisplayName("Deve refletir o valor total da ordem após adicionar itens")
    void totalValueCalculationTest() {

        OrderItem i1 = new OrderItem();
        i1.setItemPrice(new BigDecimal("100.00"));
        i1.setServicePrice(new BigDecimal("50.00"));

        OrderItem i2 = new OrderItem();
        i2.setItemPrice(new BigDecimal("200.00"));
        i2.setServicePrice(new BigDecimal("0.00"));

        orderTest.addItem(i1);
        orderTest.addItem(i2);

        BigDecimal total = orderTest.getTotalOrderValue();

        assertThat(total).isEqualByComparingTo(new BigDecimal("350.00"));
    }
}
