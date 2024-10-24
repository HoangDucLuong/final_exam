package shop.modelviews;

import org.springframework.jdbc.core.RowMapper;
import shop.model.OrderDetail;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderDetailMapper implements RowMapper<OrderDetail> {

    @Override
    public OrderDetail mapRow(ResultSet rs, int rowNum) throws SQLException {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setId(rs.getInt("id"));
        orderDetail.setOrderId(rs.getInt("order_id"));
        orderDetail.setMealId(rs.getInt("meal_id"));
        orderDetail.setQuantity(rs.getInt("quantity"));
        orderDetail.setPrice(rs.getDouble("price"));
        return orderDetail;
    }	
}
