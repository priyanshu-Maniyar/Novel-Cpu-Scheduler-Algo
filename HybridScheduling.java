package com.example;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
import java.util.*;

class Process {
    int pid;
    float arrivalTime;
    float burstTime;
    float burstTimeRemaining;
    float completionTime;
    float turnaroundTime;
    float waitingTime;
    float enter;
    float responseTime;
    boolean isComplete;
    boolean inQueue;
    boolean in;

    public Process(int pid, float arrivalTime, float burstTime) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.burstTimeRemaining = burstTime;
        this.isComplete = false;
        this.inQueue = false;
        this.in = false;
    }
}

public class HybridScheduling {

    public static void remove(int t, Queue<Integer> q) {
        Queue<Integer> ref = new LinkedList<>();
        int s = q.size();
        int cnt = 0;

        while (q.peek() != t && !q.isEmpty()) {
            ref.add(q.poll());
            cnt++;
        }

        if (q.isEmpty()) {
            while (!ref.isEmpty()) {
                q.add(ref.poll());
            }
        } else {
            q.poll();
            while (!ref.isEmpty()) {
                q.add(ref.poll());
            }
            int k = s - cnt - 1;
            while (k-- > 0) {
                int p = q.poll();
                q.add(p);
            }
        }
    }

    public static void checkForNewArrivals(Process[] processes, int n, float currentTime, Queue<Integer> readyQueue) {
        for (int i = 0; i < n; i++) {
            Process p = processes[i];

            if (p.arrivalTime <= currentTime && !p.inQueue && !p.isComplete) {
                processes[i].inQueue = true;
                readyQueue.add(i);
            }
        }
    }

    public static void updateQueue(Process[] processes, int n, float quantum, Queue<Integer> readyQueue, float currentTime, int programsExecuted, double avgBurstTime) {

        while (programsExecuted != n) {
            int i = readyQueue.poll();

            if (processes[i].burstTimeRemaining >= avgBurstTime) {
                if (!processes[i].in) {
                    processes[i].enter = currentTime;
                    processes[i].in = true;
                }

                if (processes[i].burstTimeRemaining < quantum) {
                    processes[i].isComplete = true;
                    currentTime += processes[i].burstTimeRemaining;
                    processes[i].completionTime = currentTime;
                    processes[i].waitingTime = processes[i].completionTime - processes[i].arrivalTime - processes[i].burstTime;
                    processes[i].turnaroundTime = processes[i].waitingTime + processes[i].burstTime;
                    if (processes[i].waitingTime < 0) processes[i].waitingTime = 0;
                    processes[i].responseTime = processes[i].enter - processes[i].arrivalTime;
                    if (processes[i].responseTime < 0) processes[i].responseTime = 0;
                    processes[i].burstTimeRemaining = 0;
                    programsExecuted++;
                    processes[i].inQueue = false;

                    if (programsExecuted != n) {
                        checkForNewArrivals(processes, n, currentTime, readyQueue);
                    }
                } else {
                    processes[i].burstTimeRemaining -= quantum;
                    currentTime += quantum;

                    if (programsExecuted != n) {
                        checkForNewArrivals(processes, n, currentTime, readyQueue);
                    }

                    readyQueue.add(i);
                }
            } else {
                int shortestJob = i;
                float shortestTime = processes[i].burstTimeRemaining;
                for (int j = 0; j < n; j++) {
                    if (processes[j].burstTimeRemaining < avgBurstTime && processes[j].burstTimeRemaining > 0 && processes[j].burstTimeRemaining < shortestTime && processes[j].inQueue) {
                        shortestJob = j;
                        shortestTime = processes[j].burstTimeRemaining;
                    }

                    if (processes[j].burstTimeRemaining >= avgBurstTime && processes[j].burstTimeRemaining > 0 && processes[j].inQueue) {
                        break;
                    }
                }

                if (!processes[shortestJob].in) {
                    processes[shortestJob].enter = currentTime;
                }

                processes[shortestJob].isComplete = true;
                currentTime += processes[shortestJob].burstTimeRemaining;
                processes[shortestJob].completionTime = currentTime;
                processes[shortestJob].waitingTime = processes[shortestJob].completionTime - processes[shortestJob].arrivalTime - processes[shortestJob].burstTime;
                if (processes[shortestJob].waitingTime < 0) processes[shortestJob].waitingTime = 0;
                processes[shortestJob].turnaroundTime = processes[shortestJob].waitingTime + processes[shortestJob].burstTime;
                processes[shortestJob].burstTimeRemaining = 0;
                processes[shortestJob].responseTime = processes[shortestJob].enter - processes[shortestJob].arrivalTime;
                if (processes[shortestJob].responseTime < 0) processes[shortestJob].responseTime = 0;
                programsExecuted++;

                remove(shortestJob, readyQueue);

                processes[shortestJob].inQueue = false;

                if (programsExecuted != n) {
                    checkForNewArrivals(processes, n, currentTime, readyQueue);
                }
            }
        }
    }

    public static void output(Process[] processes, int n) {
        double avgWaitingTime = 0;
        double avgTurnaroundTime = 0;
        double avgResponseTime = 0;
        Arrays.sort(processes, Comparator.comparingInt(p -> p.pid));

        System.out.println("\n\n\t\t The Process Status \n\n");
        System.out.println("\tProcess ID\tArrival Time\tBurst Time\tCompletion Time\tTurn Around Time\tWaiting Time\tResponse Time");
        for (Process process : processes) {
            System.out.printf("\n\t\t%d\t\t%.2f\t\t%.2f\t\t%.2f\t\t%.2f\t\t%.2f\t\t%.2f", process.pid, process.arrivalTime, process.burstTime,
                    process.completionTime, process.turnaroundTime, process.waitingTime, process.responseTime);
            avgWaitingTime += process.waitingTime;
            avgTurnaroundTime += process.turnaroundTime;
            avgResponseTime += process.responseTime;
        }
        avgWaitingTime /= n;
        avgTurnaroundTime /= n;
        avgResponseTime /= n;
        System.out.println("\n\n\t\tAverage Waiting Time: " + avgWaitingTime);
        System.out.println("\n\t\tAverage Turn Around Time: " + avgTurnaroundTime);
        System.out.println("\n\t\tAverage Response Time: " + avgResponseTime);
        System.out.println("\n\n\n");
    }

    public static void hybridScheduling(Process[] processes, int n, float quantum, double avgBurstTime) {
        Queue<Integer> readyQueue = new LinkedList<>();
        readyQueue.add(0);
        processes[0].inQueue = true;

        float currentTime = 0;
        int programsExecuted = 0;

        updateQueue(processes, n, quantum, readyQueue, currentTime, programsExecuted, avgBurstTime);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the number of processes: ");
        int n = scanner.nextInt();

        Process[] processes = new Process[n];

        for (int i = 0; i < n; i++) {
            System.out.print("Enter arrival time and burst time of process " + (i + 1) + ": ");
            float arrivalTime = scanner.nextFloat();
            float burstTime = scanner.nextFloat();
            processes[i] = new Process(i + 1, arrivalTime, burstTime);
        }

        Arrays.sort(processes, Comparator.comparing(p -> p.arrivalTime));

        float totalBurstTime = 0;
        for (Process process : processes) {
            totalBurstTime += process.burstTime;
        }

        double avgBurstTime = totalBurstTime / n;

        float sumBurstTimeRR = 0;
        float numRRProcesses = 0;

        for (Process process : processes) {
            if (process.burstTime >= avgBurstTime) {
                sumBurstTimeRR += process.burstTime;
                numRRProcesses++;
            }
        }

        float timeQuantum;
        if ((sumBurstTimeRR / (numRRProcesses * numRRProcesses)) > 2) {
            timeQuantum = sumBurstTimeRR / (numRRProcesses * numRRProcesses);
        } else {
            timeQuantum = 2;
        }

        System.out.println("\nAverage burst time= " + avgBurstTime);
        System.out.println("Time quantum= " + timeQuantum);

        hybridScheduling(processes, n, timeQuantum, avgBurstTime);

        output(processes, n);
    }
}
