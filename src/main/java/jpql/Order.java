package jpql;

import javax.persistence.*;

@Entity
@Table(name = "ORDERS") // order 가 sql 예약어이기 때문에 테이블명을 바꿔야 함
public class Order {

    @Id @GeneratedValue
    @Column(name = "ORDER_ID")
    private Long id;
    private int orderAmount;

    @Embedded
    private Address address;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID")
    private Product product;
}
