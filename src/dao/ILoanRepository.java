package dao;

import java.util.List;
import entity.model.Loan;
import exception.InvalidLoanException;

public interface ILoanRepository {
    void applyLoan(Loan loan) throws InvalidLoanException;
    double calculateInterest(int loanId) throws InvalidLoanException;
    double calculateInterest(double principal, double rate, int term);
    void loanStatus(int loanId) throws InvalidLoanException;
    double calculateEMI(int loanId) throws InvalidLoanException;
    double calculateEMI(double principal, double rate, int term);
    double getCalculatedInterest(int loanId) throws InvalidLoanException;
    double getCalculatedEMI(int loanId) throws InvalidLoanException;
    void loanRepayment(int loanId, double amount) throws InvalidLoanException;
    List<Loan> getAllLoan();
    Loan getLoanById(int loanId) throws InvalidLoanException;
}
