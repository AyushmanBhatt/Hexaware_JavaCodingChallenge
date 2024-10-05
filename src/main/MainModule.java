package main;

import java.util.List;
import java.util.Scanner;

import dao.ILoanRepository;
import dao.ILoanRepositoryImpl;
import entity.model.*;
import exception.InvalidLoanException;

public class MainModule {
    public static void main(String[] args) {
        try {
            ILoanRepository loanRepo = new ILoanRepositoryImpl();
            Scanner scanner = new Scanner(System.in);
            int choice = 0;

            while (choice != 8) {
                System.out.println("\n=== Loan Management System ===");
                System.out.println("1. Apply for Loan");
                System.out.println("2. Get All Loans");
                System.out.println("3. Get Loan by ID");
                System.out.println("4. Repay Loan");
                System.out.println("5. Update Loan Status");
                System.out.println("6. Calculate EMI");
                System.out.println("7. Calculate Total Interest");
                System.out.println("8. Exit");
                System.out.print("Enter your choice: ");
                choice = scanner.nextInt();
                scanner.nextLine(); // consume newline

                switch (choice) {
                    case 1:
                        applyLoan(loanRepo, scanner);
                        break;
                    case 2:
                        getAllLoans(loanRepo);
                        break;
                    case 3:
                        getLoanById(loanRepo, scanner);
                        break;
                    case 4:
                        repayLoan(loanRepo, scanner);
                        break;
                    case 5:
                        updateLoanStatus(loanRepo, scanner);
                        break;
                    case 6:
                        calculateTotalEMI(loanRepo, scanner);
                        break;
                    case 7:
                        calculateTotalInterest(loanRepo, scanner);
                        break;
                    case 8:
                        System.out.println("Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }

            scanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void applyLoan(ILoanRepository loanRepo, Scanner scanner) {
        try {
            System.out.println("\n--- Apply for Loan ---");
            // Gather Customer Details
            System.out.print("Enter Customer Name: ");
            String name = scanner.nextLine();
            System.out.print("Enter Email Address: ");
            String email = scanner.nextLine();
            System.out.print("Enter Phone Number: ");
            String phone = scanner.nextLine();
            System.out.print("Enter Address: ");
            String address = scanner.nextLine();
            System.out.print("Enter Credit Score: ");
            int creditScore = scanner.nextInt();
            scanner.nextLine(); // consume newline

            Customer customer = new Customer(0, name, email, phone, address, creditScore);

            // Gather Loan Details
            System.out.print("Enter Principal Amount: ");
            double principal = scanner.nextDouble();
            System.out.print("Enter Interest Rate (Annual %): ");
            double rate = scanner.nextDouble();
            System.out.print("Enter Loan Term (months): ");
            int term = scanner.nextInt();
            scanner.nextLine(); // consume newline
            System.out.print("Enter Loan Type (CarLoan/HomeLoan): ");
            String type = scanner.nextLine();

            Loan loan = null;

            if (type.equalsIgnoreCase("HomeLoan")) {
                System.out.print("Enter Property Address: ");
                String propAddress = scanner.nextLine();
                System.out.print("Enter Property Value: ");
                double propValue = scanner.nextDouble();
                scanner.nextLine(); // consume newline
                loan = new HomeLoan(0, customer, principal, rate, term, "HomeLoan", "Pending", propAddress, propValue);
            } else if (type.equalsIgnoreCase("CarLoan")) {
                System.out.print("Enter Car Model: ");
                String carModel = scanner.nextLine();
                System.out.print("Enter Car Value: ");
                double carValue = scanner.nextDouble();
                scanner.nextLine(); // consume newline
                loan = new CarLoan(0, customer, principal, rate, term, "CarLoan", "Pending", carModel, carValue);
            } else {
                System.out.println("Invalid Loan Type.");
                return;
            }

            // Confirm before applying
            System.out.print("Do you want to apply for this loan? (Yes/No): ");
            String confirm = scanner.nextLine();
            if (confirm.equalsIgnoreCase("Yes")) {
                loanRepo.applyLoan(loan);
            } else {
                System.out.println("Loan application canceled.");
            }
        } catch (InvalidLoanException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getAllLoans(ILoanRepository loanRepo) {
        System.out.println("\n--- All Loans ---");
        List<Loan> loans = loanRepo.getAllLoan();
        for (Loan loan : loans) {
            System.out.println(loan);
        }
    }

    private static void getLoanById(ILoanRepository loanRepo, Scanner scanner) {
        try {
            System.out.print("\nEnter Loan ID: ");
            int loanId = scanner.nextInt();
            scanner.nextLine(); // consume newline
            Loan loan = loanRepo.getLoanById(loanId);
            System.out.println(loan);
        } catch (InvalidLoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void repayLoan(ILoanRepository loanRepo, Scanner scanner) {
        try {
            System.out.print("\nEnter Loan ID to Repay: ");
            int loanId = scanner.nextInt();
            System.out.print("Enter Repayment Amount: ");
            double amount = scanner.nextDouble();
            scanner.nextLine(); // consume newline
            loanRepo.loanRepayment(loanId, amount);
        } catch (InvalidLoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void updateLoanStatus(ILoanRepository loanRepo, Scanner scanner) {
        try {
            System.out.print("\nEnter Loan ID to Update Status: ");
            int loanId = scanner.nextInt();
            scanner.nextLine(); // consume newline
            loanRepo.loanStatus(loanId);
        } catch (InvalidLoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    private static void calculateTotalEMI(ILoanRepository loanRepo, Scanner scanner) {
        try {
            System.out.print("\nEnter Loan ID to Calculate EMI: ");
            int loanId = scanner.nextInt();
            Loan loan = loanRepo.getLoanById(loanId);
            double emi = calculateEMI(loan);
            System.out.printf("Calculated EMI for Loan ID %d: %.2f\n", loanId, emi);
        } catch (InvalidLoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static double calculateEMI(Loan loan) {
        double principal = loan.getPrincipalAmount();
        double annualRate = loan.getInterestRate();
        int termInMonths = loan.getLoanTerm();
        
        // Monthly interest rate
        double monthlyRate = annualRate / (12 * 100);
        
        // EMI Calculation
        double emi = (principal * monthlyRate * Math.pow(1 + monthlyRate, termInMonths)) / 
                     (Math.pow(1 + monthlyRate, termInMonths) - 1);
        
        return emi;
    }

    private static void calculateTotalInterest(ILoanRepository loanRepo, Scanner scanner) {
        try {
            System.out.print("\nEnter Loan ID to Calculate Total Interest: ");
            int loanId = scanner.nextInt();
            Loan loan = loanRepo.getLoanById(loanId);
            double totalInterest = calculateInterest(loan);
            System.out.printf("Total Interest for Loan ID %d: %.2f\n", loanId, totalInterest);
        } catch (InvalidLoanException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static double calculateInterest(Loan loan) {
    	double principal = loan.getPrincipalAmount();
        double annualRate = loan.getInterestRate();
        int termInMonths = loan.getLoanTerm();

        // Total interest calculation
        double totalInterest = ((principal * annualRate * termInMonths) / 100) / 12; // Annual rate divided by 12 for monthly interest
        return totalInterest;
    }
}
