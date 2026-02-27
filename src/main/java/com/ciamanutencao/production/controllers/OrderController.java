package com.ciamanutencao.production.controllers;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ciamanutencao.production.dto.OrderDTO;
import com.ciamanutencao.production.dto.OrderItemDTO;
import com.ciamanutencao.production.services.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping(value = "/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTO dto) {
        OrderDTO orderDTO = orderService.createOrder(dto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(orderDTO.id())
                .toUri();
        return ResponseEntity.created(uri).body(orderDTO);
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> findAllOrders() {
        List<OrderDTO> list = orderService.findAllOrders();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<OrderDTO> findOrderById(@PathVariable Long id) {
        OrderDTO dto = orderService.findOrderById(id);
        return ResponseEntity.ok().body(dto);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id,
            @Valid @RequestBody OrderDTO dto) {
        dto = orderService.updateOrder(id, dto);
        return ResponseEntity.ok().body(dto);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/finalize")
    public ResponseEntity<OrderDTO> finalizeOrder(@RequestParam String orderNumber) {
        OrderDTO orderCompleted = orderService.finalizeOrder(orderNumber, LocalDateTime.now());
        return ResponseEntity.ok().body(orderCompleted);
    }

    @PostMapping(value = "/additems/{id}")
    public ResponseEntity<OrderDTO> addItemsFromOrder(@PathVariable Long id,
            @RequestBody List<OrderItemDTO> orderItemDTO) {
        OrderDTO order = orderService.addItemsFromOrder(id, orderItemDTO);
        return ResponseEntity.ok().body(order);
    }

    @DeleteMapping(value = "/removeitems/{id}")
    public ResponseEntity<OrderDTO> removeItemsFromOrder(@PathVariable Long id,
            @RequestBody List<Integer> itemNumbers) {
        OrderDTO order = orderService.removeItemsFromOrder(id, itemNumbers);
        return ResponseEntity.ok().body(order);
    }

}