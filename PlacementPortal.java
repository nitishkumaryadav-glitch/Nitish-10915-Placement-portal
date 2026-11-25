import java.io.*;
import java.util.*;

public class PlacementPortal {
    private static final String DATA_FILE = "placements.txt";

    // Student profile
    static class Student {
        String id, name;
        double cgpa;

        Student(String id, String name, double cgpa) {
            this.id = id;
            this.name = name;
            this.cgpa = cgpa;
        }
    }

    // BST node: students are stored using student ID
    static class Node {
        Student data;
        Node left, right;

        Node(Student data) {
            this.data = data;
        }
    }

    // Job details
    static class Job {
        String id, company, role;
        double minCgpa;

        Job(String id, String company, String role, double minCgpa) {
            this.id = id;
            this.company = company;
            this.role = role;
            this.minCgpa = minCgpa;
        }
    }

    private static Node root;
    // HashMap gives job lookup by job ID
    private static final HashMap<String, Job> jobs = new HashMap<>();
    // One set per job prevents a student applying twice to the same job
    private static final HashMap<String, HashSet<String>> applications = new HashMap<>();
    private static final Scanner sc = new Scanner(System.in);

    // Insert a student into the BST
    static Node insertStudent(Node node, Student student) {
        if (node == null) return new Node(student);

        int compare = student.id.compareToIgnoreCase(node.data.id);
        if (compare < 0) {
            node.left = insertStudent(node.left, student);
        } else if (compare > 0) {
            node.right = insertStudent(node.right, student);
        }
        return node;
    }

    // Search a student in the BST
    static Student searchStudent(Node node, String id) {
        while (node != null) {
            int compare = id.compareToIgnoreCase(node.data.id);
            if (compare == 0) return node.data;
            node = (compare < 0) ? node.left : node.right;
        }
        return null;
    }

    static void addStudent() {
        System.out.print("Student ID: ");
        String id = sc.nextLine().trim();

        if (id.isEmpty() || searchStudent(root, id) != null) {
            System.out.println("Invalid or duplicate student ID.");
            return;
        }

        System.out.print("Student name: ");
        String name = sc.nextLine().trim();

        double cgpa = readDouble("CGPA (0 to 10): ");
        if (name.isEmpty() || cgpa < 0 || cgpa > 10) {
            System.out.println("Invalid student details.");
            return;
        }

        root = insertStudent(root, new Student(id, name, cgpa));
        System.out.println("Student added successfully.");
    }

    static void addJob() {
        System.out.print("Job ID: ");
        String id = sc.nextLine().trim();

        if (id.isEmpty() || jobs.containsKey(id)) {
            System.out.println("Invalid or duplicate job ID.");
            return;
        }

        System.out.print("Company name: ");
        String company = sc.nextLine().trim();
        System.out.print("Job role: ");
        String role = sc.nextLine().trim();
        double minCgpa = readDouble("Minimum CGPA (0 to 10): ");

        if (company.isEmpty() || role.isEmpty() || minCgpa < 0 || minCgpa > 10) {
            System.out.println("Invalid job details.");
            return;
        }

        jobs.put(id, new Job(id, company, role, minCgpa));
        applications.put(id, new HashSet<String>());
        System.out.println("Job added successfully.");
    }

    static void searchJob() {
        System.out.print("Enter Job ID: ");
        String id = sc.nextLine().trim();

        Job job = jobs.get(id); // HashMap lookup
        if (job == null) {
            System.out.println("Job " + id + " not found.");
        } else {
            printJob(job);
        }
    }

    static void applyForJob() {
        System.out.print("Student ID: ");
        String studentId = sc.nextLine().trim();
        Student student = searchStudent(root, studentId);

        if (student == null) {
            System.out.println("Student not found. Add the student first.");
            return;
        }

        System.out.print("Job ID: ");
        String jobId = sc.nextLine().trim();
        Job job = jobs.get(jobId);

        if (job == null) {
            System.out.println("Job " + jobId + " not found.");
            return;
        }

        if (student.cgpa < job.minCgpa) {
            System.out.println("Not eligible. Required CGPA: " + job.minCgpa);
            return;
        }

        HashSet<String> appliedStudents = applications.get(jobId);
        if (appliedStudents.contains(studentId)) {
            System.out.println("Duplicate application not allowed.");
            return;
        }

        appliedStudents.add(studentId);
        System.out.println("Application submitted successfully.");
    }

    // Higher CGPA gets higher priority
    static void showShortlist() {
        System.out.print("Enter Job ID: ");
        String jobId = sc.nextLine().trim();
        Job job = jobs.get(jobId);

        if (job == null) {
            System.out.println("Job " + jobId + " not found.");
            return;
        }

        HashSet<String> applied = applications.get(jobId);
        PriorityQueue<Student> pq = new PriorityQueue<>(
            (a, b) -> Double.compare(b.cgpa, a.cgpa)
        );

        for (String studentId : applied) {
            Student student = searchStudent(root, studentId);
            if (student != null && student.cgpa >= job.minCgpa) {
                pq.offer(student);
            }
        }

        if (pq.isEmpty()) {
            System.out.println("No eligible applications for this job.");
            return;
        }

        // Extract from PriorityQueue, then use sorting for final display
        ArrayList<Student> shortlist = new ArrayList<>();
        while (!pq.isEmpty()) {
            shortlist.add(pq.poll());
        }

        // Sorting: CGPA descending; ID ascending if CGPA is equal
        shortlist.sort((a, b) -> {
            int byCgpa = Double.compare(b.cgpa, a.cgpa);
            return (byCgpa != 0) ? byCgpa : a.id.compareToIgnoreCase(b.id);
        });

        System.out.println("\nShortlist for " + job.company + " - " + job.role);
        System.out.println("-----------------------------------------");
        int rank = 1;
        for (Student student : shortlist) {
            System.out.printf("%d. %s | %s | CGPA %.2f%n",
                rank++, student.id, student.name, student.cgpa);
        }
    }

    static void viewStudent() {
        System.out.print("Enter Student ID: ");
        String id = sc.nextLine().trim();
        Student student = searchStudent(root, id);

        if (student == null) {
            System.out.println("Student not found.");
        } else {
            System.out.printf("ID: %s | Name: %s | CGPA: %.2f%n",
                student.id, student.name, student.cgpa);
        }
    }

    static void viewJobs() {
        if (jobs.isEmpty()) {
            System.out.println("No jobs available.");
            return;
        }
        for (Job job : jobs.values()) {
            printJob(job);
        }
    }

    static void printJob(Job job) {
        System.out.printf("Job ID: %s | Company: %s | Role: %s | Minimum CGPA: %.2f%n",
            job.id, job.company, job.role, job.minCgpa);
    }

    // Load students, jobs and applications when the program starts
    static void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|", -1);

                try {
                    if (p[0].equals("STU") && p.length == 4) {
                        Student s = new Student(p[1], p[2], Double.parseDouble(p[3]));
                        if (searchStudent(root, s.id) == null) {
                            root = insertStudent(root, s);
                        }
                    } else if (p[0].equals("JOB") && p.length == 5) {
                        Job j = new Job(p[1], p[2], p[3], Double.parseDouble(p[4]));
                        jobs.put(j.id, j);
                        applications.putIfAbsent(j.id, new HashSet<String>());
                    } else if (p[0].equals("APP") && p.length == 3) {
                        applications.putIfAbsent(p[1], new HashSet<String>());
                        applications.get(p[1]).add(p[2]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Skipped invalid data line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Could not load data: " + e.getMessage());
        }
    }

    // Save all data when the user exits
    static void saveData() {
        try (PrintWriter out = new PrintWriter(new FileWriter(DATA_FILE))) {
            saveStudentsInOrder(root, out);

            for (Job job : jobs.values()) {
                out.println("JOB|" + clean(job.id) + "|" + clean(job.company) + "|"
                    + clean(job.role) + "|" + job.minCgpa);
            }

            for (Map.Entry<String, HashSet<String>> entry : applications.entrySet()) {
                for (String studentId : entry.getValue()) {
                    out.println("APP|" + clean(entry.getKey()) + "|" + clean(studentId));
                }
            }
            System.out.println("Data saved in " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("Could not save data: " + e.getMessage());
        }
    }

    static void saveStudentsInOrder(Node node, PrintWriter out) {
        if (node == null) return;
        saveStudentsInOrder(node.left, out);
        Student s = node.data;
        out.println("STU|" + clean(s.id) + "|" + clean(s.name) + "|" + s.cgpa);
        saveStudentsInOrder(node.right, out);
    }

    // Keep the file format safe if a user enters a pipe character
    static String clean(String value) {
        return value.replace("|", "/").replace("\n", " ").replace("\r", " ");
    }

    static double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    static void menu() {
        System.out.println("\n====== STUDENT PLACEMENT PORTAL ======");
        System.out.println("1. Add Student");
        System.out.println("2. Add Job");
        System.out.println("3. Search Job");
        System.out.println("4. Apply for Job");
        System.out.println("5. View Shortlist");
        System.out.println("6. Search Student");
        System.out.println("7. View All Jobs");
        System.out.println("0. Save and Exit");
        System.out.print("Enter choice: ");
    }

    public static void main(String[] args) {
        loadData();
        System.out.println("Placement data loaded.");

        while (true) {
            menu();
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "1": addStudent(); break;
                case "2": addJob(); break;
                case "3": searchJob(); break;
                case "4": applyForJob(); break;
                case "5": showShortlist(); break;
                case "6": viewStudent(); break;
                case "7": viewJobs(); break;
                case "0":
                    saveData();
                    System.out.println("Thank you for using Placement Portal.");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
