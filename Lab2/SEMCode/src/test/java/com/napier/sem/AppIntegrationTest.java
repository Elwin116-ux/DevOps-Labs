
package com.napier.sem;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lab 7 - Integration Testing.
 *
 * Tests database operations using the
 * MySQL employees database.
 */
public class AppIntegrationTest
{
    static App app;

    /**
     * Establish a database connection before all tests.
     */
    @BeforeAll
    static void init()
    {
        app = new App();
        app.connect("localhost:33060", 30000);
    }

    /**
     * Test 1: Retrieve an existing employee.
     */
    @Test
    void testGetEmployee()
    {
        Employee emp = app.getEmployee(255530);

        assertNotNull(
                emp,
                "Employee should exist in the database"
        );

        assertEquals(255530, emp.emp_no);
        assertEquals("Ronghao", emp.first_name);
        assertEquals("Garigliano", emp.last_name);
    }

    /**
     * Test 2: Retrieve the Sales department.
     */
    @Test
    void testGetDepartment()
    {
        Department dept = app.getDepartment("Sales");

        assertNotNull(
                dept,
                "Sales department should exist"
        );

        assertEquals("Sales", dept.dept_name);
        assertNotNull(dept.dept_no);
    }

    /**
     * Test 3: Retrieve current salaries
     * for employees in the Sales department.
     */
    @Test
    void testGetSalariesByDepartment()
    {
        Department dept = app.getDepartment("Sales");

        assertNotNull(
                dept,
                "Sales department should exist"
        );

        ArrayList<Employee> employees =
                app.getSalariesByDepartment(dept);

        assertNotNull(
                employees,
                "Employee salary list should not be null"
        );

        assertFalse(
                employees.isEmpty(),
                "Sales department should contain employees"
        );

        for (Employee emp : employees)
        {
            assertNotNull(emp);
            assertTrue(emp.emp_no > 0);
            assertNotNull(emp.first_name);
            assertNotNull(emp.last_name);
            assertTrue(emp.salary > 0);

            assertNotNull(
                    emp.dept,
                    "Employee should have a department"
            );

            assertEquals(
                    dept.dept_no,
                    emp.dept.dept_no
            );
        }
    }

    /**
     * Test 4: Retrieve all current employee salaries.
     */
    @Test
    void testGetAllSalaries()
    {
        ArrayList<Employee> employees = app.getAllSalaries();

        assertNotNull(
                employees,
                "Employee salary list should not be null"
        );

        assertFalse(
                employees.isEmpty(),
                "Database should contain employee salaries"
        );

        Employee firstEmployee = employees.get(0);

        assertNotNull(firstEmployee);
        assertTrue(firstEmployee.emp_no > 0);
        assertNotNull(firstEmployee.first_name);
        assertNotNull(firstEmployee.last_name);
        assertTrue(firstEmployee.salary > 0);
    }

    /**
     * Test 5: Add a new employee to the database.
     *
     * Lab 7 - Test-Driven Development.
     *
     * Uses a temporary employee number and removes
     * the inserted employee after the test.
     */
    @Test
    void testAddEmployee() throws SQLException
    {
        int employeeNo = ThreadLocalRandom.current()
                .nextInt(900000, 1000000);

        Employee emp = new Employee();
        emp.emp_no = employeeNo;
        emp.first_name = "Kevin";
        emp.last_name = "Chalmers";

        boolean inserted = false;

        try
        {
            // Ensure the temporary employee number is unused.
            assertNull(
                    app.getEmployee(employeeNo),
                    "Temporary employee number already exists"
            );

            // Insert the new employee.
            app.addEmployee(emp);
            inserted = true;

            // Retrieve the inserted employee.
            Employee addedEmployee =
                    app.getEmployee(employeeNo);

            assertNotNull(
                    addedEmployee,
                    "New employee should exist in the database"
            );

            assertEquals(
                    employeeNo,
                    addedEmployee.emp_no
            );

            assertEquals(
                    "Kevin",
                    addedEmployee.first_name
            );

            assertEquals(
                    "Chalmers",
                    addedEmployee.last_name
            );
        }
        finally
        {
            if (inserted)
            {
                // Remove only the temporary record created
                // by this test, not existing employee data.
                String sql =
                        "DELETE FROM employees " +
                                "WHERE emp_no = ? " +
                                "AND first_name = ? " +
                                "AND last_name = ?";

                try (Connection cleanupCon =
                             DriverManager.getConnection(
                                     "jdbc:mysql://localhost:33060/"
                                             + "employees?allowPublicKeyRetrieval=true"
                                             + "&useSSL=false",
                                     "root",
                                     "example"
                             );
                     PreparedStatement stmt =
                             cleanupCon.prepareStatement(sql))
                {
                    stmt.setInt(1, employeeNo);
                    stmt.setString(2, "Kevin");
                    stmt.setString(3, "Chalmers");

                    assertEquals(
                            1,
                            stmt.executeUpdate(),
                            "Temporary employee should be removed"
                    );
                }
            }
        }
    }

    /**
     * Disconnect from MySQL after all tests.
     */
    @AfterAll
    static void close()
    {
        if (app != null)
        {
            app.disconnect();
        }
    }
}
