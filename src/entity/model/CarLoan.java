package entity.model;

public class CarLoan extends Loan {
    private String carModel;
    private double carValue;

    // Default Constructor
    public CarLoan() {
        super();
    }

    // Parameterized Constructor
    public CarLoan(int loanId, Customer customer, double principalAmount, double interestRate, int loanTerm,
            String loanType, String loanStatus, String carModel, double carValue) {
        super(loanId, customer, principalAmount, interestRate, loanTerm, loanType, loanStatus);
        this.carModel = carModel;
        this.carValue = carValue;
    }

    // Getters and Setters
    public String getCarModel() {
        return carModel;
    }
    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }
    public double getCarValue() {
        return carValue;
    }
    public void setCarValue(double carValue) {
        this.carValue = carValue;
    }

    // toString Method
    @Override
    public String toString() {
        return super.toString() + ", CarLoan [Car Model=" + carModel + ", Car Value=" + carValue + "]";
    }
}
