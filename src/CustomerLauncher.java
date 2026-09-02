import PageManager.switchPage;
import views.InfoView;
import views.LoginView;
import views.RegisterView;

import javax.swing.*;
import java.awt.*;

public class CustomerLauncher extends JFrame{
    public CustomerLauncher() {
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardLayout cardLayout = new CardLayout();
        switchPage.cardLayout = cardLayout;
        switchPage.container = new JPanel(cardLayout);


        switchPage.container.add(new LoginView(false), "LOGIN");
        switchPage.container.add(new RegisterView(), "REGISTER");

        switchPage.container.add(new InfoView("Scam Alert", "🛡️", new String[]{
                "<b>1. Savings Account</b><br>Earn up to 3.8% p.a. interest with NexusSave.",
                "<b>2. Current Account</b><br>Zero monthly fees and unlimited online transfers.",
                "<b>3. Fixed Deposit</b><br>Secure your future with guaranteed high returns.",
                "<b>4. Islamic Banking</b><br>Shariah-compliant accounts with competitive profit rates."
        }), "SCAM_ALERT");

        switchPage.container.add(new InfoView("Accounts", "🏦", new String[]{
                "<b>1. Savings Account</b><br>Earn up to 3.8% p.a. interest with NexusSave.",
                "<b>2. Current Account</b><br>Zero monthly fees and unlimited online transfers.",
                "<b>3. Fixed Deposit</b><br>Secure your future with guaranteed high returns.",
                "<b>4. Islamic Banking</b><br>Shariah-compliant accounts with competitive profit rates."
        }), "ACCOUNTS_INFO");

        switchPage.container.add(new InfoView("Cards", "💳", new String[]{
                "<b>1. Credit Cards</b><br>5% cashback on dining and petrol.",
                "<b>2. Debit Cards</b><br>Worldwide acceptance and instant frozen via app.",
                "<b>3. Virtual Card</b><br>Shop online securely with dynamic CVV.",
                "<b>4. Nexus Elite</b><br>Exclusive travel perks and airport lounge access. Earn 3x Reward Points for overseas spending."

        }), "CARDS_INFO");

        switchPage.container.add(new InfoView("Loans & Financing", "💰", new String[]{
                "<b>1. Personal Loan</b><br>Fast approval with rates as low as 4.5%.",
                "<b>2. Home Financing</b><br>Flexible repayment schemes for your dream home.",
                "<b>3. Car Loan</b><br>Competitive rates for both new and used vehicles.",
                "<b>4. Education Loan</b><br>Empowering your future with easy installments."
        }), "LOAN_FINANCING");

        switchPage.container.add(new InfoView("Insurance", "🩺", new String[]{
                "<b>1. Life Insurance</b><br>Protect your loved ones' future.",
                "<b>2. Medical Card</b><br>Comprehensive coverage at all major hospitals.",
                "<b>3. Motor Insurance</b><br>Instant road tax renewal and 24/7 towing.",
                "<b>4. Travel Insurance</b><br>Global coverage for a worry-free journey."
        }), "INSURANCE_INFO");

        switchPage.container.add(new InfoView("Wealth", "🌱", new String[]{
                "<b>1. Unit Trust Investments</b><br>Access a diversified portfolio managed by professionals to grow your capital.",
                "<b>2. Gold Investment Account</b><br>Hedge against inflation by investing in 99.9% pure gold without the hassle of physical storage.",
                "<b>3. Private Retirement Schemes (PRS)</b><br>Voluntary long-term investment scheme designed to help you save more for retirement.",
                "<b>4. Nexus Robo-Advisory</b><br>Automated, algorithm-based financial planning with personalized investment advice."
        }), "WEALTH_INFO");

        switchPage.container.add(new InfoView("Financial Relief", "🤝", new String[]{
                "<b>1. Payment Assistance</b><br>Flexible repayment plans with reduced monthly payments during temporary difficulties.",
                "<b>2. Debt Consolidation</b><br>Combine multiple high-interest debts into one simpler, more manageable monthly payment.",
                "<b>3. Disaster Relief</b><br>Financial support and payment relief options for customers affected by natural disasters.",
                "<b>4. Financial Counseling</b><br>Access to expert financial advice for guidance during difficult times and future planning."
        }), "FINANCIAL_RELIEF");

        switchPage.container.add(new InfoView("Online Trading", "📈", new String[]{
                "<b>1. NexusTrade Platform</b><br>Real-time access to Bursa Malaysia with advanced charting tools and instant execution.",
                "<b>2. Global Markets</b><br>Trade stocks and ETFs in major global exchanges including the NYSE, NASDAQ, and HKEX.",
                "<b>3. Shariah-Compliant Stocks</b><br>Dedicated filters to help you identify and trade only Shariah-compliant investment products.",
                "<b>4. Integrated Portfolio Management</b><br>Track your investments and performance with our all-in-one dashboard."
        }), "ONLINE_TRADING");

        add(switchPage.container);
        setVisible(true);
    }

    public static void main(String[] args) {
        new CustomerLauncher();
    }
}