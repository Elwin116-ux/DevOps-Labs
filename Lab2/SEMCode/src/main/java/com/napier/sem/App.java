package com.napier.sem;

import java.sql.*;
import java.util.ArrayList;

public class App
{
    /**
     * Connection to MySQL database.
     */
    private Connection con = null;

    /**
     * Connect to the MySQL database.
     */
    public void connect()
    {
        try
        {
            // Load Database driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;

        for (int i = 0; i < retries; ++i)
        {
            System.out.println("Connecting to database...");

            try
            {
                // Wait for database to start
                Thread.sleep(30000);

                // Connect to database
                con = DriverManager.getConnection(
                        "jdbc:mysql://db:3306/employees?useSSL=false",
                        "root",
                        "example"
                );

                System.out.println("Successfully connected");
                break;
            }
            catch (SQLException sqle)
            {
                System.out.println(
                        "Failed to connect to database attempt " + i
                );
                System.out.println(sqle.getMessage());
            }
            catch (InterruptedException ie)
            {
                System.out.println(
                        "Thread interrupted? Should not happen."
                );
            }
        }
    }

    /**
     * Disconnect from the MySQL database.
     */
    public void disconnect()
    {
        if (con != null)
        {
            try
            {
                con.close();
            }
            catch (Exception e)
            {
                System.out.println(
                        "Error closing connection to database"
                );
            }
        }
    }

    /**
     * Gets all current employees and salaries.
     *
     * @return A list of all employees and salaries, or null if there is an error.
     */
    public ArrayList<Employee> getAllSalaries()
    {
        try
        {
            Statement stmt = con.createStatement();

            String strSelect =
                    "SELECT employees.emp_no, employees.first_name, employees.last_name, salaries.salary "
                            + "FROM employees, salaries "
                            + "WHERE employees.emp_no = salaries.emp_no "
                            + "AND salaries.to_date = '9999-01-01' "
                            + "ORDER BY employees.emp_no ASC";

            ResultSet rset = stmt.executeQuery(strSelect);

            ArrayList<Employee> employees = new ArrayList<Employee>();

            while (rset.next())
            {
                Employee emp = new Employee();

                emp.emp_no = rset.getInt("employees.emp_no");
                emp.first_name = rset.getString("employees.first_name");
                emp.last_name = rset.getString("employees.last_name");
                emp.salary = rset.getInt("salaries.salary");

                employees.add(emp);
            }

            return employees;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get salary details");
            return null;
        }
    }

    /**
     * Prints a list of employees and salaries.
     *
     * @param employees The list of employees to print.
     */
    public void printSalaries(ArrayList<Employee> employees)
    {
        // Print header
        System.out.println(
                String.format(
                        "%-10s %-15s %-20s %-8s",
                        "Emp No",
                        "First Name",
                        "Last Name",
                        "Salary"
                )
        );

        // Loop over all employees in the list
        for (Employee emp : employees)
        {
            String empString =
                    String.format(
                            "%-10s %-15s %-20s %-8s",
                            emp.emp_no,
                            emp.first_name,
                            emp.last_name,
                            emp.salary
                    );

            System.out.println(empString);
        }
    }

    /**
     * Get employee details from the database.
     *
     * @param ID Employee number
     * @return Employee object or null if not found
     */
    public Employee getEmployee(int ID)
    {
        try
        {
            Statement stmt = con.createStatement();

            String strSelect =
                    "SELECT e.emp_no, e.first_name, e.last_name, "
                            + "t.title, s.salary, d.dept_name, "
                            + "CONCAT(m.first_name, ' ', m.last_name) AS manager "
                            + "FROM employees e "
                            + "JOIN titles t ON e.emp_no = t.emp_no "
                            + "JOIN salaries s ON e.emp_no = s.emp_no "
                            + "JOIN dept_emp de ON e.emp_no = de.emp_no "
                            + "JOIN departments d ON de.dept_no = d.dept_no "
                            + "JOIN dept_manager dm ON d.dept_no = dm.dept_no "
                            + "JOIN employees m ON dm.emp_no = m.emp_no "
                            + "WHERE e.emp_no = " + ID + " "
                            + "AND t.to_date = '9999-01-01' "
                            + "AND s.to_date = '9999-01-01' "
                            + "AND de.to_date = '9999-01-01' "
                            + "AND dm.to_date = '9999-01-01'";

            ResultSet rset = stmt.executeQuery(strSelect);

            if (rset.next())
            {
                Employee emp = new Employee();

                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");
                emp.title = rset.getString("title");
                emp.salary = rset.getInt("salary");
                Department dept = new Department();
                dept.dept_name = rset.getString("dept_name");
                emp.dept = dept;

                Employee manager = new Employee();
                manager.first_name = rset.getString("manager");
                emp.manager = manager;
                dept.manager = manager;
                return emp;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employee details");
            return null;
        }
    }

    /**
     * Retrieve a department by its name.
     *
     * @param dept_name Name of the department.
     * @return Department object or null if not found.
     */
    public Department getDepartment(String dept_name) {

        String sql =
                "SELECT d.dept_no, d.dept_name, "
                        + "m.emp_no AS manager_no, "
                        + "m.first_name AS manager_first_name, "
                        + "m.last_name AS manager_last_name "
                        + "FROM departments d "
                        + "LEFT JOIN dept_manager dm "
                        + "ON d.dept_no = dm.dept_no "
                        + "AND dm.to_date = '9999-01-01' "
                        + "LEFT JOIN employees m ON dm.emp_no = m.emp_no "
                        + "WHERE d.dept_name = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, dept_name);

            try (ResultSet rset = stmt.executeQuery()) {

                if (rset.next()) {
                    Department dept = new Department();

                    dept.dept_no = rset.getString("dept_no");
                    dept.dept_name = rset.getString("dept_name");

                    int managerNo = rset.getInt("manager_no");

                    if (!rset.wasNull()) {
                        Employee manager = new Employee();

                        manager.emp_no = managerNo;
                        manager.first_name =
                                rset.getString("manager_first_name");
                        manager.last_name =
                                rset.getString("manager_last_name");

                        dept.manager = manager;
                    }

                    return dept;
                }

                return null;
            }

        } catch (SQLException e) {
            System.out.println("Failed to get department: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieve current employee salaries for a department.
     *
     * @param dept Department to retrieve salaries for.
     * @return List of employees, or null if an error occurs.
     */
    public ArrayList<Employee> getSalariesByDepartment(Department dept)
    {
        if (dept == null || dept.dept_no == null)
        {
            System.out.println("Invalid department.");
            return null;
        }

        String sql =
                "SELECT e.emp_no, e.first_name, e.last_name, s.salary "
                        + "FROM employees e "
                        + "JOIN salaries s ON e.emp_no = s.emp_no "
                        + "JOIN dept_emp de ON e.emp_no = de.emp_no "
                        + "WHERE s.to_date = '9999-01-01' "
                        + "AND de.to_date = '9999-01-01' "
                        + "AND de.dept_no = ? "
                        + "ORDER BY e.emp_no ASC";

        try (PreparedStatement stmt = con.prepareStatement(sql))
        {
            stmt.setString(1, dept.dept_no);

            try (ResultSet rset = stmt.executeQuery())
            {
                ArrayList<Employee> employees = new ArrayList<>();

                while (rset.next())
                {
                    Employee emp = new Employee();

                    emp.emp_no = rset.getInt("emp_no");
                    emp.first_name = rset.getString("first_name");
                    emp.last_name = rset.getString("last_name");
                    emp.salary = rset.getInt("salary");

                    emp.dept = dept;
                    emp.manager = dept.manager;

                    employees.add(emp);
                }

                return employees;
            }
        }
        catch (SQLException e)
        {
            System.out.println(
                    "Failed to get department salaries: " + e.getMessage()
            );
            return null;
        }
    }

    /**
     * Display employee details.
     *
     * @param emp Employee to display
     */

    public void displayEmployee(Employee emp)
    {
        if (emp != null)
        {
            System.out.println("Employee Number: " + emp.emp_no);
            System.out.println("Name: " + emp.first_name + " " + emp.last_name);
            System.out.println("Title: " + emp.title);
            System.out.println("Salary: " + emp.salary);
            System.out.println("Department: " +
                    (emp.dept != null ? emp.dept.dept_name : "N/A"));

            System.out.println("Manager: " +
                    (emp.manager != null
                            ? emp.manager.first_name + " " + emp.manager.last_name
                            : "N/A"));
        }
        else
        {
            System.out.println("Employee not found");

        }
    }


    public static void main(String[] args)
    {
        // Create application
        App a = new App();

        // Connect to database
        a.connect();

        try
        {
            // Retrieve Sales department
            Department sales = a.getDepartment("Sales");

            if (sales != null)
            {
                System.out.println("Department: " + sales.dept_name);

                // Display department manager
                if (sales.manager != null)
                {
                    System.out.println("Manager: "
                            + sales.manager.first_name + " "
                            + sales.manager.last_name);
                }

                // Retrieve salaries for Sales department
                ArrayList<Employee> employees =
                        a.getSalariesByDepartment(sales);

                // Display results
                if (employees != null)
                {
                    a.printSalaries(employees);
                }
            }
            else
            {
                System.out.println("Sales department not found.");
            }
        }
        finally
        {
            // Disconnect from database
            a.disconnect();
        }
    }

}