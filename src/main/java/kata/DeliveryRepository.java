package kata;

import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.jdbc.runtime.JdbcOperations;
import io.micronaut.data.model.query.builder.sql.Dialect;

import javax.transaction.Transactional;
import javax.validation.constraints.Email;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@JdbcRepository(dialect = Dialect.H2)
public class DeliveryRepository {

    private final JdbcOperations jdbcOperations;

    public DeliveryRepository(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    @Transactional
    public void create( String email, float lat, float lon) {
        String sql =
                """
                        INSERT INTO Delivery (email, date_of_order, latitude, longitude)
                        VALUES ('%s',CURRENT_TIMESTAMP(),%s,%s);
                        """.formatted(email, lat, lon);
        jdbcOperations.prepareStatement(sql, statement -> {
                    statement.execute();
                    return null;
                }
        );
    }

    @Transactional
    public List<Delivery> findTodaysDeliveries() {
        String sql = "SELECT * FROM Delivery WHERE FORMATDATETIME(date_of_order, 'yyyy-MM-dd') = CURRENT_DATE()";
        return jdbcOperations.prepareStatement(sql, statement -> {
            ResultSet rs = statement.executeQuery();
            List<Delivery> result = new ArrayList<>();
            while (rs.next()) {
                long id = rs.getLong("id");
                String email = rs.getString("email");
                float latitude = rs.getFloat("latitude");
                float longitude = rs.getFloat("longitude");
                Timestamp orderedAt = rs.getTimestamp("date_of_order");

                Delivery delivery = new Delivery(id, email, latitude, longitude, orderedAt.toLocalDateTime(), false, false);
                result.add(delivery);
            }
            return result;
        });
    }

    @Transactional
    public void save(Delivery delivery) {
        String sql = "UPDATE Delivery SET date_of_delivery = ?, arrived = ?, onTime = ? where id = ?";
        jdbcOperations.prepareStatement(sql, statement -> {
                    statement.setTimestamp(1, java.sql.Timestamp.valueOf(delivery.getTimeOfDelivery()));
                    statement.setBoolean(2, delivery.isArrived());
                    statement.setBoolean(3, delivery.isOnTime());
                    statement.setLong(4, delivery.getId());
                    statement.execute();
                    return null;
                }
        );
    }
}
