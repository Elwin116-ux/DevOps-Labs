
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
     * Connect to the MySQL database at the supplied host and port.
     * Wait only after a failed attempt, to support fast local debugging.
     *
     * @param location Database hostname and port
     *                 (e.g. localhost:33060 or db:3306).
     * @param delay Milliseconds to wait between failed attempts.
     */
    public void connect(String location, int delay)
    {
        try
        {
            // Load MySQL Connector/J driver.
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;
        boolean shouldWait = false;

        for (int i = 0; i < retries; ++i)
        {
            System.out.println("Connecting to database...");

            try
            {
                if (shouldWait)
                {
                    Thread.sleep(delay);
                }

                con = DriverManager.getConnection(
                        "jdbc:mysql://" + location
                                + "/employees?allowPublicKeyRetrieval=true&useSSL=false",
                        "root",
                        "example"
                );

                System.out.println("Successfully connected");
                break;
            }
            catch (SQLException sqle)
            {
                System.out.println(
                        "Failed to connect to database attempt " + (i + 1)
                );
                System.out.println(sqle.getMessage());
                shouldWait = true;
            }
            catch (InterruptedException ie)
            {
                Thread.currentThread().interrupt();
                System.out.println("Database connection retry interrupted");
                break;
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
     * @return A list of all employees and salaries,
     *         or null if there is an error.
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
        if (employees == null)
        {
            System.out.println("No employees");
            return;
        }

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
            // Skip null employee records
            if (emp == null)
            {
                continue;
            }

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
     * Uses LEFT JOIN so a newly inserted employee can
     * be retrieved even without a salary, title or department.
     *
     * @param ID Employee number
     * @return Employee object or null if not found
     */
    public Employee getEmployee(int ID)
    {
        String sql =
                "SELECT e.emp_no, e.first_name, e.last_name, "
                        + "t.title, s.salary, d.dept_no, d.dept_name, "
                        + "m.emp_no AS manager_no, "
                        + "m.first_name AS manager_first_name, "
                        + "m.last_name AS manager_last_name "
                        + "FROM employees e "
                        + "LEFT JOIN titles t ON e.emp_no = t.emp_no "
                        + "AND t.to_date = '9999-01-01' "
                        + "LEFT JOIN salaries s ON e.emp_no = s.emp_no "
                        + "AND s.to_date = '9999-01-01' "
                        + "LEFT JOIN dept_emp de ON e.emp_no = de.emp_no "
                        + "AND de.to_date = '9999-01-01' "
                        + "LEFT JOIN departments d ON de.dept_no = d.dept_no "
                        + "LEFT JOIN dept_manager dm ON d.dept_no = dm.dept_no "
                        + "AND dm.to_date = '9999-01-01' "
                        + "LEFT JOIN employees m ON dm.emp_no = m.emp_no "
                        + "WHERE e.emp_no = ?";

        try (PreparedStatement stmt = con.prepareStatement(sql))
        {
            stmt.setInt(1, ID);

            try (ResultSet rset = stmt.executeQuery())
            {
                if (!rset.next())
                {
                    return null;
                }

                Employee emp = new Employee();

                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");
                emp.title = rset.getString("title");
                emp.salary = rset.getInt("salary");

                String deptNo = rset.getString("dept_no");

                if (deptNo != null)
                {
                    Department dept = new Department();

                    dept.dept_no = deptNo;
                    dept.dept_name = rset.getString("dept_name");
                    emp.dept = dept;

                    int managerNo = rset.getInt("manager_no");

                    if (!rset.wasNull())
                    {
                        Employee manager = new Employee();

                        manager.emp_no = managerNo;
                        manager.first_name =
                                rset.getString("manager_first_name");
                        manager.last_name =
                                rset.getString("manager_last_name");

                        emp.manager = manager;
                        dept.manager = manager;
                    }
                }

                return emp;
            }
        }
        catch (SQLException e)
        {
            System.out.println(
                    "Failed to get employee details: " + e.getMessage()
            );
            return null;
        }
    }

    /**
     * Retrieve a department by its name.
     *
     * @param dept_name Name of the department.
     * @return Department object or null if not found.
     */
    public Department getDepartment(String dept_name)
    {
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

        try (PreparedStatement stmt = con.prepareStatement(sql))
        {
            stmt.setString(1, dept_name);

            try (ResultSet rset = stmt.executeQuery())
            {
                if (rset.next())
                {
                    Department dept = new Department();

                    dept.dept_no = rset.getString("dept_no");
                    dept.dept_name = rset.getString("dept_name");

                    int managerNo = rset.getInt("manager_no");

                    if (!rset.wasNull())
                    {
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
        }
        catch (SQLException e)
        {
            System.out.println(
                    "Failed to get department: " + e.getMessage()
            );
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

            System.out.println(
                    "Name: " + emp.first_name + " " + emp.last_name
            );

            System.out.println("Title: " + emp.title);
            System.out.println("Salary: " + emp.salary);

            System.out.println(
                    "Department: "
                            + (emp.dept != null
                            ? emp.dept.dept_name
                            : "N/A")
            );

            System.out.println(
                    "Manager: "
                            + (emp.manager != null
                            ? emp.manager.first_name + " "
                            + emp.manager.last_name
                            : "N/A")
            );
        }
        else
        {
            System.out.println("Employee not found");
        }
    }

    /**
     * Start the application using local or Docker database settings.
     *
     * Local default: localhost:33060
     * Docker example: db:3306 10000
     */
    public static void main(String[] args)
    {
        // Create application
        App a = new App();

        // Use local defaults or command-line parameters.
        String location = "localhost:33060";
        int delay = 10000;

        if (args.length == 2)
        {
            location = args[0];

            try
            {
                delay = Integer.parseInt(args[1]);

                if (delay < 0)
                {
                    System.err.println("Delay must not be negative.");
                    return;
                }
            }
            catch (NumberFormatException e)
            {
                System.err.println(
                        "Delay must be an integer in milliseconds."
                );
                return;
            }
        }
        else if (args.length != 0)
        {
            System.err.println(
                    "Usage: App [host:port delayMilliseconds]"
            );
            return;
        }

        // Connect to database
        a.connect(location, delay);

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
                    System.out.println(
                            "Manager: "
                                    + sales.manager.first_name + " "
                                    + sales.manager.last_name
                    );
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

    /**
     * Lab 7 - Add a new employee to the MySQL employees table.
     *
     * The dates and gender are temporary sample values for
     * this laboratory exercise because the integration test
     * supplies only an employee number and name.
     *
     * @param emp Employee to add
     */
    public void addEmployee(Employee emp)
    {
        if (emp == null)
        {
            throw new IllegalArgumentException(
                    "Employee must not be null"
            );
        }

        String sql =
                "INSERT INTO employees "
                        + "(emp_no, first_name, last_name, birth_date, gender, hire_date) "
                        + "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = con.prepareStatement(sql))
        {
            stmt.setInt(1, emp.emp_no);
            stmt.setString(2, emp.first_name);
            stmt.setString(3, emp.last_name);
            stmt.setDate(4, Date.valueOf("1990-01-01"));
            stmt.setString(5, "M");
            stmt.setDate(6, Date.valueOf("2020-01-01"));

            stmt.executeUpdate();

            System.out.println("Employee added successfully.");
        }
        catch (SQLException e)
        {
            System.out.println(
                    "Failed to add employee: " + e.getMessage()
            );

            throw new IllegalStateException(
                    "Unable to add employee", e
            );
        }
    }
}
