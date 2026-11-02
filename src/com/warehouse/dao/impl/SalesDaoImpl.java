
package com.warehouse.dao.impl;

import com.warehouse.config.DBConnection;
import com.warehouse.dao.SalesDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SalesDaoImpl implements SalesDao {

    @Override
    public double getTotalRevenue() {
        String sql = "SELECT SUM(oi.quantity * p.price) AS revenue " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "JOIN orders o ON oi.order_id = o.order_id " +
                "WHERE o.status = 'FULFILLED'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("revenue");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<Map<String, Object>> getTopSellingProducts() {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = "SELECT p.product_name, SUM(oi.quantity) AS total_sold " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "JOIN orders o ON oi.order_id = o.order_id " +
                "WHERE o.status = 'FULFILLED' " +
                "GROUP BY p.product_id, p.product_name " +
                "ORDER BY total_sold DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("product_name", rs.getString("product_name"));
                row.put("total_sold", rs.getInt("total_sold"));
                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Map<String, Object>> getSalesReport() {
        List<Map<String, Object>> list = new ArrayList<>();

        String sql = "SELECT o.order_date, SUM(oi.quantity * p.price) AS revenue " +
                "FROM orders o " +
                "JOIN order_items oi ON o.order_id = oi.order_id " +
                "JOIN products p ON oi.product_id = p.product_id " +
                "WHERE o.status = 'FULFILLED' " +
                "GROUP BY o.order_date " +
                "ORDER BY o.order_date";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("order_date", rs.getDate("order_date"));
                row.put("revenue", rs.getDouble("revenue"));
                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
