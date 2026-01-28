package ui.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

// Simple DTO to represent an Employee (row in table).
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    private String firstName;
    private String lastName;
    private String email;
    private String age;
    private String salary;
    private String department;
}