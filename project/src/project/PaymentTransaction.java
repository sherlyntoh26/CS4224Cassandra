package project;

import java.math.BigDecimal;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class PaymentTransaction {
	// private attributes
	private Session session;
	private PreparedStatement selectWarehouseStmt;
	private PreparedStatement updateWarehouseStmt;

	public PaymentTransaction() {

	}

	public PaymentTransaction(Connection connection) {
		session = connection.getSession();
		selectWarehouseStmt = session.prepare("SELECT wdi_w_ytd, ? FROM warehouseDistrictInfo WHERE wdi_w_id = ?;");
		updateWarehouseStmt = session.prepare("UPDATE warehouseDistrictInfo SET wdi_w_ytd = ?, ? = ? WHERE wdi_w_id = ?;");
	}

	public void makePayment(int cWID, int cDID, int cID, float payment) {

		String wdi_d_ytd;
		if (cDID == 10) {
			wdi_d_ytd = "wdi_d_ytd_10";
		} else {
			wdi_d_ytd = "wdi_d_ytd_0" + cDID;
		}

		// update warehouseDistrictInfo by increasing wdi_w_ytd and wdi_d_ytd_# by payment
		ResultSet warehouseResult = session.execute(selectWarehouseStmt.bind(wdi_d_ytd, cWID));
		Row warehouseRow = warehouseResult.one();
		float w_ytd = warehouseRow.getDecimal("wdi_w_ytd").floatValue();
		float d_ytd = warehouseRow.getDecimal(wdi_d_ytd).floatValue();
		w_ytd += payment;
		d_ytd += payment;
		session.execute(updateWarehouseStmt.bind(BigDecimal.valueOf(w_ytd), wdi_d_ytd, BigDecimal.valueOf(d_ytd), cWID));

	}

	// for payment transaction running alone.
	// debuggin use
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Connection connection = new Connection();
		connection.connect("127.0.0.1", "project");
		PaymentTransaction payment = new PaymentTransaction(connection);

		payment.makePayment(1, 1, 331, 1.02f);
	}

}
