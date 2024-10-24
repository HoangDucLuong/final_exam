package shop.modelviews;

import org.springframework.jdbc.core.RowMapper;
import shop.model.Order;
import shop.model.OrderDetail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OrderMapper implements RowMapper<Order> {

    @Override
    public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setContractId(rs.getInt("contract_id"));
        
        Timestamp orderDateTimestamp = rs.getTimestamp("order_date");
        if (orderDateTimestamp != null) {
            order.setOrderDate(orderDateTimestamp.toLocalDateTime());
        } else {
            order.setOrderDate(null);
        }

        order.setStatus(rs.getInt("status"));

        // Bạn có thể thêm mã để lấy danh sách OrderDetail ở đây nếu cần
        List<OrderDetail> orderDetails = new ArrayList<>(); // Có thể cần điều chỉnh
        // Ví dụ: order.setOrderDetails(orderDetails);

        return order;
    }
}
