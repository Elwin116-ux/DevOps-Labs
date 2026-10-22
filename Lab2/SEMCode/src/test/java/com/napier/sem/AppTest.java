
package com.napier.sem;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Lab 6 - Unit Testing.
 *
 * Tests the printSalaries() and displayEmployee()
 * methods of the App class.
 */
public class AppTest
{
    static App app;

    /**
     * Initialize the application before running tests.
     */
    @BeforeAll
    static void init()
    {
        app = new App();
    }

    /**
     * Capture the output printed by an application method.
     *
     * @param action Method to execute.
     * @return Captured console output.
     */
    private String captureOutput(Runnable action)
    {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try
        {
            System.setOut(
                    new PrintStream(
                            output,
                            true,
                            StandardCharsets.UTF_8
                    )
            );

            action.run();
        }
        finally
        {
            System.setOut(originalOut);
        }

        return output.toString(StandardCharsets.UTF_8);
    }

    /**
     * Test 1: Verify that a null employee list
     * is handled correctly.
     */
    @Test
    void printSalariesTestNull()
    {
        String output = assertDoesNotThrow(
                () -> captureOutput(
                        () -> app.printSalaries(null)
                )
        );

        assertEquals("No employees", output.trim());
    }

    /**
     * Test 2: Verify that an empty employee list
     * prints the table header without employee records.
     */
    @Test
    void printSalariesTestEmpty()
    {
        ArrayList<Employee> employees = new ArrayList<>();

        String output = assertDoesNotThrow(
                () -> captureOutput(
                        () -> app.printSalaries(employees)
                )
        );

        assertTrue(output.contains("Emp No"));
        assertTrue(output.contains("First Name"));
        assertTrue(output.contains("Last Name"));
        assertTrue(output.contains("Salary"));

        assertFalse(output.contains("Kevin"));
    }

    /**
     * Test 3: Verify that a null employee inside
     * a list is skipped safely.
     */
    @Test
    void printSalariesTestContainsNull()
    {
        ArrayList<Employee> employees = new ArrayList<>();

        employees.add(null);

        String output = assertDoesNotThrow(
                () -> captureOutput(
                        () -> app.printSalaries(employees)
                )
        );

        assertTrue(output.contains("Emp No"));
        assertTrue(output.contains("Salary"));
        assertFalse(output.contains("null"));
    }

    /**
     * Test 4: Verify salary details for
     * a valid employee.
     */
    @Test
    void printSalariesTestNormal()
    {
        ArrayList<Employee> employees = new ArrayList<>();

        Employee emp = new Employee();

        emp.emp_no = 1;
        emp.first_name = "Kevin";
        emp.last_name = "Chalmers";
        emp.title = "Engineer";
        emp.salary = 55000;

        employees.add(emp);

        String output = captureOutput(
                () -> app.printSalaries(employees)
        );

        assertTrue(output.contains("Kevin"));
        assertTrue(output.contains("Chalmers"));
        assertTrue(output.contains("55000"));
        assertTrue(output.contains("1"));
    }

    /**
     * Test 5: Verify that displaying a null
     * employee produces the expected message.
     */
    @Test
    void displayEmployeeTestNull()
    {
        String output = assertDoesNotThrow(
                () -> captureOutput(
                        () -> app.displayEmployee(null)
                )
        );

        assertEquals("Employee not found", output.trim());
    }

    /**
     * Test 6: Verify all displayed information
     * for a valid employee.
     */
    @Test
    void displayEmployeeTestValid()
    {
        Employee emp = new Employee();

        emp.emp_no = 1;
        emp.first_name = "Kevin";
        emp.last_name = "Chalmers";
        emp.title = "Engineer";
        emp.salary = 55000;

        Department dept = new Department();
        dept.dept_name = "Sales";
        emp.dept = dept;

        Employee manager = new Employee();
        manager.first_name = "John";
        manager.last_name = "Smith";
        emp.manager = manager;

        String output = captureOutput(
                () -> app.displayEmployee(emp)
        );

        assertTrue(output.contains("Employee Number: 1"));
        assertTrue(output.contains("Name: Kevin Chalmers"));
        assertTrue(output.contains("Title: Engineer"));
        assertTrue(output.contains("Salary: 55000"));
        assertTrue(output.contains("Department: Sales"));
        assertTrue(output.contains("Manager: John Smith"));
    }
}
