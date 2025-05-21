package com.example.orderservice.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Payment mode is required")
    private String paymentMode;

    @NotBlank(message = "Delivery address is required")
    private String deliveryAddress;

    private String orderStatus;

    @CreationTimestamp
    private LocalDateTime orderDate;

    private double totalPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
}
