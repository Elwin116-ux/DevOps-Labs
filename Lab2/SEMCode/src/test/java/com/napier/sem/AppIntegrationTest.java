
package com.napier.sem;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

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
        // Retrieve the Sales department.
        Department dept = app.getDepartment("Sales");

        assertNotNull(
                dept,
                "Sales department should exist"
        );

        // Retrieve employees and salaries.
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

        // Verify returned employee records.
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
