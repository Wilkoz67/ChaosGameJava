# Chaos Game 2D

> Real-time visualization of fractal attractors using the Chaos Game algorithm, implemented in Java with a Swing GUI.

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/GUI-Swing-007396?style=flat-square&logo=java&logoColor=white)

---

## About the Project

The **Chaos Game** is an iterative algorithm that generates fractal structures by repeatedly applying random transformations to a single point. Despite the apparent randomness, the process converges to a deterministic fractal attractor — the shape depends entirely on the ruleset used.

This project implements the algorithm with a Java Swing interface that renders the attractor in real time, allowing exploration of different fractal configurations.

---

## Features

- Real-time fractal rendering on a canvas
- Multiple built-in presets (Sierpinski triangle, Barnsley fern, and more)
- Interactive Java Swing GUI
- Clean OOP architecture — algorithms separated from rendering logic

---

## Tech Stack

- **Language:** Java 17+
- **GUI:** Java Swing
- **Architecture:** Object-Oriented Programming

---

## Project Structure

```
ChaosGameJava/
├── src/               # Java source files
├── run.bat            # Windows launcher
├── run.sh             # Linux / macOS launcher
└── README.md
```

---

## Running the Project

**Requirements:** Java 17+

**Windows:**
```bat
run.bat
```

**Linux / macOS:**
```bash
./run.sh
```

---

## How the Algorithm Works

1. Choose a set of transformation rules (e.g., vertices of a triangle)
2. Start from a random initial point
3. At each step: pick a random rule and apply the transformation
4. Plot each resulting point
5. After thousands of iterations, the fractal attractor emerges

The key insight: **deterministic structure emerges from random choices** — a fundamental property of iterated function systems (IFS).

---

## Author

**Marek Jancevic** · [GitHub: Wilkoz67](https://github.com/Wilkoz67)
