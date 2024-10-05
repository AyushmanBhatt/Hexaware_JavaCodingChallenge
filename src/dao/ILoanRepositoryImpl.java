package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import entity.model.*;
import exception.InvalidLoanException;
import util.DBConnUtil;
import util.DBPropertyUtil;

public class ILoanRepositoryImpl implements ILoanRepository {

    private Connection conn;

    public ILoanRepositoryImpl() throws SQLException {
        // Initialize the connection using utility classes
        String connStr = DBPropertyUtil.getConnectionString("db.properties");
        this.conn = DBConnUtil.getDBConn(connStr);
    }

    @Override
    public void applyLoan(Loan loan) throws InvalidLoanException {
        try {
            // Start transaction
            conn.setAutoCommit(false);

            // Insert customer if not exists
            Customer customer = loan.getCustomer();
            int customerId = customer.getCustomerId();

            if (customerId == 0) { // Assuming 0 means new customer
                String insertCustomerSQL = "INSERT INTO Customer (name, email, phone_number, address, credit_score) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(insertCustomerSQL, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, customer.getName());
                ps.setString(2, customer.getEmail());
                ps.setString(3, customer.getPhoneNumber());
                ps.setString(4, customer.getAddress());
                ps.setInt(5, customer.getCreditScore());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    customerId = rs.getInt(1);
                    customer.setCustomerId(customerId);
                }
                rs.close();
                ps.close();
            }

            // Insert loan
            String insertLoanSQL = "INSERT INTO Loan (customer_id, principal_amount, interest_rate, loan_term, loan_type, loan_status) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement psLoan = conn.prepareStatement(insertLoanSQL, Statement.RETURN_GENERATED_KEYS);
            psLoan.setInt(1, customerId);
            psLoan.setDouble(2, loan.getPrincipalAmount());
            psLoan.setDouble(3, loan.getInterestRate());
            psLoan.setInt(4, loan.getLoanTerm());
            psLoan.setString(5, loan.getLoanType());
            psLoan.setString(6, "Pending"); // Initially Pending
            psLoan.executeUpdate();

            ResultSet rsLoan = psLoan.getGeneratedKeys();
            int loanId = 0;
            if (rsLoan.next()) {
                loanId = rsLoan.getInt(1);
                loan.setLoanId(loanId);
                loan.setLoanStatus("Pending");
            }
            rsLoan.close();
            psLoan.close();

            // Insert into specific loan table
            if (loan instanceof HomeLoan) {
                HomeLoan homeLoan = (HomeLoan) loan;
                String insertHomeLoanSQL = "INSERT INTO HomeLoan (loan_id, property_address, property_value) VALUES (?, ?, ?)";
                PreparedStatement psHome = conn.prepareStatement(insertHomeLoanSQL);
                psHome.setInt(1, loanId);
                psHome.setString(2, homeLoan.getPropertyAddress());
                psHome.setDouble(3, homeLoan.getPropertyValue());
                psHome.executeUpdate();
                psHome.close();
            } else if (loan instanceof CarLoan) {
                CarLoan carLoan = (CarLoan) loan;
                String insertCarLoanSQL = "INSERT INTO CarLoan (loan_id, car_model, car_value) VALUES (?, ?, ?)";
                PreparedStatement psCar = conn.prepareStatement(insertCarLoanSQL);
                psCar.setInt(1, loanId);
                psCar.setString(2, carLoan.getCarModel());
                psCar.setDouble(3, carLoan.getCarValue());
                psCar.executeUpdate();
                psCar.close();
            }

            // Commit transaction
            conn.commit();
            conn.setAutoCommit(true);
            System.out.println("Loan applied successfully with Loan ID: " + loanId);
        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new InvalidLoanException("Failed to apply for loan: " + e.getMessage());
        }
    }

    @Override
    public double calculateInterest(int loanId) throws InvalidLoanException {
        Loan loan = getLoanById(loanId);
        return calculateInterest(loan.getPrincipalAmount(), loan.getInterestRate(), loan.getLoanTerm());
    }

    @Override
    public double calculateInterest(double principal, double rate, int term) {
        return (principal * rate * term) / 12;
    }

    @Override
    public void loanStatus(int loanId) throws InvalidLoanException {
        try {
            Loan loan = getLoanById(loanId);
            Customer customer = loan.getCustomer();
            String status = customer.getCreditScore() > 650 ? "Approved" : "Rejected";

            String updateStatusSQL = "UPDATE Loan SET loan_status = ? WHERE loan_id = ?";
            PreparedStatement ps = conn.prepareStatement(updateStatusSQL);
            ps.setString(1, status);
            ps.setInt(2, loanId);
            ps.executeUpdate();
            ps.close();

            loan.setLoanStatus(status);
            System.out.println("Loan ID " + loanId + " has been " + status);
        } catch (SQLException e) {
            throw new InvalidLoanException("Failed to update loan status: " + e.getMessage());
        }
    }

    @Override
    public double calculateEMI(int loanId) throws InvalidLoanException {
        Loan loan = getLoanById(loanId);
        return calculateEMI(loan.getPrincipalAmount(), loan.getInterestRate(), loan.getLoanTerm());
    }

    @Override
    public double calculateEMI(double principal, double rate, int term) {
        double monthlyRate = rate / (12 * 100);
        double emi = (principal * monthlyRate * Math.pow(1 + monthlyRate, term)) / (Math.pow(1 + monthlyRate, term) - 1);
        return emi;
    }
    
    @Override
    public double getCalculatedInterest(int loanId) throws InvalidLoanException {
        Loan loan = getLoanById(loanId);
        return calculateInterest(loan.getPrincipalAmount(), loan.getInterestRate(), loan.getLoanTerm());
    }

    @Override
    public double getCalculatedEMI(int loanId) throws InvalidLoanException {
        Loan loan = getLoanById(loanId);
        return calculateEMI(loan.getPrincipalAmount(), loan.getInterestRate(), loan.getLoanTerm());
    }


    @Override
    public void loanRepayment(int loanId, double amount) throws InvalidLoanException {
        try {
            Loan loan = getLoanById(loanId);
            double emi = calculateEMI(loanId);
            if (amount < emi) {
                System.out.println("Repayment amount is less than a single EMI. Payment rejected.");
                return;
            }
            // Check if loan status is "Rejected"
            if (loan.getLoanStatus().equalsIgnoreCase("Rejected")) {
                System.out.println("Loan has been rejected. Cannot process repayment.");
                return;
            }

            // Here, you'd typically have a repayment table. For simplicity, let's assume we're tracking remaining EMIs in the Loan table.
            // First, add a new column to Loan table: remaining_emi INT

            // Check if 'remaining_emi' exists; if not, add it
            DatabaseMetaData dbm = conn.getMetaData();
            ResultSet rs = dbm.getColumns(null, null, "Loan", "remaining_emi");
            if (!rs.next()) {
                String alterTableSQL = "ALTER TABLE Loan ADD COLUMN remaining_emi INT";
                Statement stmt = conn.createStatement();
                stmt.execute(alterTableSQL);
                stmt.close();
            }
            rs.close();

            // Initialize remaining_emi if not set
            if (loan.getLoanTerm() > 0) { // Assuming loanTerm is remaining_emis
                String checkEmiSQL = "SELECT remaining_emi FROM Loan WHERE loan_id = ?";
                PreparedStatement psCheck = conn.prepareStatement(checkEmiSQL);
                psCheck.setInt(1, loanId);
                ResultSet rsCheck = psCheck.executeQuery();
                if (rsCheck.next()) {
                    int remainingEmi = rsCheck.getInt("remaining_emi");
                    if (remainingEmi == 0) { // Initialize
                        remainingEmi = loan.getLoanTerm();
                        String initEmiSQL = "UPDATE Loan SET remaining_emi = ? WHERE loan_id = ?";
                        PreparedStatement psInit = conn.prepareStatement(initEmiSQL);
                        psInit.setInt(1, remainingEmi);
                        psInit.setInt(2, loanId);
                        psInit.executeUpdate();
                        psInit.close();
                    }
                    rsCheck.close();
                    psCheck.close();

                    // Calculate number of EMIs that can be paid
                    int emisToPay = (int)(amount / emi);
                    if (emisToPay <= 0) {
                        System.out.println("Amount is less than one EMI. Payment rejected.");
                        return;
                    }

                    // Update remaining EMIs
                    remainingEmi -= emisToPay;
                    if (remainingEmi < 0) remainingEmi = 0;

                    String updateEmiSQL = "UPDATE Loan SET remaining_emi = ? WHERE loan_id = ?";
                    PreparedStatement psUpdate = conn.prepareStatement(updateEmiSQL);
                    psUpdate.setInt(1, remainingEmi);
                    psUpdate.setInt(2, loanId);
                    psUpdate.executeUpdate();
                    psUpdate.close();

                    System.out.println("Repayment successful. Number of EMIs paid: " + emisToPay);
                    System.out.println("Remaining EMIs: " + remainingEmi);
                }
            }

        } catch (SQLException e) {
            throw new InvalidLoanException("Failed to process loan repayment: " + e.getMessage());
        }
    }

    @Override
    public List<Loan> getAllLoan() {
        List<Loan> loans = new ArrayList<>();
        try {
            String getAllLoanSQL = "SELECT * FROM Loan";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(getAllLoanSQL);

            while (rs.next()) {
                int loanId = rs.getInt("loan_id");
                int customerId = rs.getInt("customer_id");
                double principal = rs.getDouble("principal_amount");
                double interestRate = rs.getDouble("interest_rate");
                int term = rs.getInt("loan_term");
                String type = rs.getString("loan_type");
                String status = rs.getString("loan_status");

                // Get Customer Details
                Customer customer = getCustomerById(customerId);

                Loan loan;
                if (type.equalsIgnoreCase("HomeLoan")) {
                    String getHomeLoanSQL = "SELECT * FROM HomeLoan WHERE loan_id = ?";
                    PreparedStatement psHome = conn.prepareStatement(getHomeLoanSQL);
                    psHome.setInt(1, loanId);
                    ResultSet rsHome = psHome.executeQuery();
                    if (rsHome.next()) {
                        String propertyAddress = rsHome.getString("property_address");
                        double propertyValue = rsHome.getDouble("property_value");
                        loan = new HomeLoan(loanId, customer, principal, interestRate, term, type, status, propertyAddress, propertyValue);
                    } else {
                        loan = new Loan();
                    }
                    rsHome.close();
                    psHome.close();
                } else if (type.equalsIgnoreCase("CarLoan")) {
                    String getCarLoanSQL = "SELECT * FROM CarLoan WHERE loan_id = ?";
                    PreparedStatement psCar = conn.prepareStatement(getCarLoanSQL);
                    psCar.setInt(1, loanId);
                    ResultSet rsCar = psCar.executeQuery();
                    if (rsCar.next()) {
                        String carModel = rsCar.getString("car_model");
                        double carValue = rsCar.getDouble("car_value");
                        loan = new CarLoan(loanId, customer, principal, interestRate, term, type, status, carModel, carValue);
                    } else {
                        loan = new Loan();
                    }
                    rsCar.close();
                    psCar.close();
                } else {
                    loan = new Loan(loanId, customer, principal, interestRate, term, type, status);
                }

                loans.add(loan);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return loans;
    }

    @Override
    public Loan getLoanById(int loanId) throws InvalidLoanException {
        try {
            String getLoanSQL = "SELECT * FROM Loan WHERE loan_id = ?";
            PreparedStatement ps = conn.prepareStatement(getLoanSQL);
            ps.setInt(1, loanId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int customerId = rs.getInt("customer_id");
                double principal = rs.getDouble("principal_amount");
                double interestRate = rs.getDouble("interest_rate");
                int term = rs.getInt("loan_term");
                String type = rs.getString("loan_type");
                String status = rs.getString("loan_status");

                // Get Customer Details
                Customer customer = getCustomerById(customerId);

                Loan loan;
                if (type.equalsIgnoreCase("HomeLoan")) {
                    String getHomeLoanSQL = "SELECT * FROM HomeLoan WHERE loan_id = ?";
                    PreparedStatement psHome = conn.prepareStatement(getHomeLoanSQL);
                    psHome.setInt(1, loanId);
                    ResultSet rsHome = psHome.executeQuery();
                    if (rsHome.next()) {
                        String propertyAddress = rsHome.getString("property_address");
                        double propertyValue = rsHome.getDouble("property_value");
                        loan = new HomeLoan(loanId, customer, principal, interestRate, term, type, status, propertyAddress, propertyValue);
                    } else {
                        throw new InvalidLoanException("HomeLoan details not found for Loan ID: " + loanId);
                    }
                    rsHome.close();
                    psHome.close();
                } else if (type.equalsIgnoreCase("CarLoan")) {
                    String getCarLoanSQL = "SELECT * FROM CarLoan WHERE loan_id = ?";
                    PreparedStatement psCar = conn.prepareStatement(getCarLoanSQL);
                    psCar.setInt(1, loanId);
                    ResultSet rsCar = psCar.executeQuery();
                    if (rsCar.next()) {
                        String carModel = rsCar.getString("car_model");
                        double carValue = rsCar.getDouble("car_value");
                        loan = new CarLoan(loanId, customer, principal, interestRate, term, type, status, carModel, carValue);
                    } else {
                        throw new InvalidLoanException("CarLoan details not found for Loan ID: " + loanId);
                    }
                    rsCar.close();
                    psCar.close();
                } else {
                    loan = new Loan(loanId, customer, principal, interestRate, term, type, status);
                }

                rs.close();
                ps.close();
                return loan;
            } else {
                throw new InvalidLoanException("Loan ID " + loanId + " not found.");
            }
        } catch (SQLException e) {
            throw new InvalidLoanException("Error retrieving loan: " + e.getMessage());
        }
    }

    private Customer getCustomerById(int customerId) throws SQLException {
        String getCustomerSQL = "SELECT * FROM Customer WHERE customer_id = ?";
        PreparedStatement psCustomer = conn.prepareStatement(getCustomerSQL);
        psCustomer.setInt(1, customerId);
        ResultSet rsCustomer = psCustomer.executeQuery();
        Customer customer = null;
        if (rsCustomer.next()) {
            customer = new Customer();
            customer.setCustomerId(rsCustomer.getInt("customer_id"));
            customer.setName(rsCustomer.getString("name"));
            customer.setEmail(rsCustomer.getString("email"));
            customer.setPhoneNumber(rsCustomer.getString("phone_number"));
            customer.setAddress(rsCustomer.getString("address"));
            customer.setCreditScore(rsCustomer.getInt("credit_score"));
        }
        rsCustomer.close();
        psCustomer.close();
        return customer;
    }
}
