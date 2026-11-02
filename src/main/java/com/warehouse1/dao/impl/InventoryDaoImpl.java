import com.warehouse1.dao.InventoryDao;
import com.warehouse1.model.OrderItem;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

@Repository
public class InventoryDaoImpl implements InventoryDao {

    private final JdbcTemplate jdbcTemplate;

    public InventoryDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<Integer, Integer> findStockForUpdate(Collection<Integer> skuIds) {
        String placeholders = String.join(",", Collections.nCopies(skuIds.size(), "?"));
        String sql = "SELECT sku_id, stock_qty FROM products WHERE sku_id IN ("
                + placeholders + ") FOR UPDATE";

        return jdbcTemplate.query(sql, rs -> {
            Map<Integer, Integer> stock = new HashMap<>();
            while (rs.next()) {
                stock.put(rs.getInt("sku_id"), rs.getInt("stock_qty"));
            }
            return stock;
        }, skuIds.toArray());
    }

    @Override
    public int[] deductStockBatch(List<OrderItem> items) {
        String sql = "UPDATE products SET stock_qty = stock_qty - ? "
                + "WHERE sku_id = ? AND stock_qty >= ?";

        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                OrderItem item = items.get(i);
                ps.setInt(1, item.getQuantity());
                ps.setInt(2, item.getSkuId());
                ps.setInt(3, item.getQuantity());
            }

            @Override
            public int getBatchSize() {
                return items.size();
            }
        });
    }
}