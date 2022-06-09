package com.indiduck.panda.Repository;

import com.indiduck.panda.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail,Long> {


    Optional<OrderDetail> findByUserAndOptionsAndOrderStatus(User user, ProductOption option,OrderStatus orderStatus);


    Optional<List<OrderDetail>> findByUserAndOrderStatus(User user, OrderStatus orderStatus);
    Optional<List<OrderDetail>> findByUserAndOrderStatusAndShop(User user, OrderStatus orderStatus, Shop shop);
    Optional<List<OrderDetail>> findByUserAndOrderStatusAndOptions_Sales(User user, OrderStatus orderStatus,Boolean OptionsSales);
    Optional<List<OrderDetail>> findByUserAndOrderStatusAndShopAndOptions_Sales(User user, OrderStatus orderStatus, Shop shop,Boolean OptionsSales);
    Optional<List<OrderDetail>> findOrderDetailByOrderStatusAndOptions_Id(OrderStatus orderStatus,Long oid);
    Optional<List<OrderDetail>> findOrderDetailByUser(User user);
    Optional<List<OrderDetail>> findOrderDetailByUserAndOrderStatus(User user,OrderStatus od);
    Optional<List<OrderDetail>> findOrderDetailByUserAndOrderStatusAndOrderStatus(User user,OrderStatus od,OrderStatus od2);
    Optional<List<OrderDetail>> findByPandaAndPaymentStatusOrPaymentStatusAndFinishedAtBetween(Panda panda, PaymentStatus paymentStatus,PaymentStatus paymentStatusse, LocalDateTime fromDate, LocalDateTime toDate);
    Optional<List<OrderDetail>> findByPandaAndPaymentStatusOrPaymentStatus(Panda panda, PaymentStatus paymentStatus,PaymentStatus paymentStatusse);
    Optional<List<OrderDetail>> findByPandaAndPaymentStatusOrPaymentStatusOrPaymentStatusAndFinishedAtBetween(Panda panda, PaymentStatus paymentStatus,PaymentStatus paymentStatusse,PaymentStatus paymentStatusthird, LocalDateTime fromDate, LocalDateTime toDate);
    Optional<List<OrderDetail>> findByPandaAndPaymentStatusAndFinishedAtBetween(Panda panda, PaymentStatus paymentStatus, LocalDateTime fromDate, LocalDateTime toDate);
    Optional<List<OrderDetail>> findByPandaAndPaymentStatusNotNullAndFinishedAtBetween(Panda panda, LocalDateTime fromDate, LocalDateTime toDate);
    Optional<List<OrderDetail>> findByPandaAndOrderStatusAndPaymentStatusAndEnrollSettle(Panda panda,OrderStatus od,PaymentStatus py,boolean en);
    //    Optional<List<OrderDetail>> findOrderDetailsByFinishedAtBetween(LocalDateTime fromDate, LocalDateTime toDate);

}
