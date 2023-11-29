import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

record Homework(String content, int timeLimit) {
}

class Student implements Runnable {
    private final String name;
    private final BlockingQueue<Homework> queue;

    public Student(String name, BlockingQueue<Homework> queue) {
        this.name = name;
        this.queue = queue;
    }

    public void submitHomework(Homework homework) throws InterruptedException {
        queue.put(homework);
        System.out.println(name + " submitted " + homework.content() + " homework");
    }

    @Override
    public void run() {
        Random random = new Random();

        while (true) {
            Homework homework = new Homework(
                    "Task " + (random.nextInt(10000) + 1),
                    random.nextInt(9001) + 1000
            );

            try {
                submitHomework(homework);
                Thread.sleep(random.nextInt(3000) + 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Grader implements Runnable {
    private final String name;
    private final BlockingQueue<Homework> queue;
    private final Semaphore semaphore;

    public Grader(String name, BlockingQueue<Homework> queue, Semaphore semaphore) {
        this.name = name;
        this.queue = queue;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        while (true) {
            try {
                semaphore.acquire();
                Homework homework = queue.poll();

                if (homework != null) {
                    gradeHomework(homework);
                }

                semaphore.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void gradeHomework(Homework homework) throws InterruptedException {
        Thread.sleep(10);
        System.out.println(name + " is grading " + homework.content() + " homework from student");
        // Simulate grading process
        Thread.sleep(2000);
        System.out.println(name + " finished grading " + homework.content() + " homework");
    }
}

class HomeworkSystem {
    public final BlockingQueue<Homework> homeworkQueue;
    private final Semaphore gradingSemaphore;

    public HomeworkSystem() {
        this.homeworkQueue = new LinkedBlockingQueue<>();
        this.gradingSemaphore = new Semaphore(3);
    }

    public void startGraders(int numGraders) {
        for (int i = 1; i <= numGraders; i++) {
            Grader grader = new Grader("Grader " + i, homeworkQueue, gradingSemaphore);
            new Thread(grader).start();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        HomeworkSystem homeworkSystem = new HomeworkSystem();
        homeworkSystem.startGraders(3);

        Student student1 = new Student("1", homeworkSystem.homeworkQueue);
        Student student2 = new Student("2", homeworkSystem.homeworkQueue);

        new Thread(student1).start();
        new Thread(student2).start();
    }
}