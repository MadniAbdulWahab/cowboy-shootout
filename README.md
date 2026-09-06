# Cowboy Shootout

A Java 21 command-line simulation of the Wild West shootout described in the test task. The program accepts the number of cowboys, runs the game until one remains, writes every shot to a UTF-8 JSON protocol, and prints its SHA-256 checksum.

## Requirements

- JDK 21
- No separate Maven installation is required; the Maven Wrapper is included.

For IntelliJ IDEA, open the project directory or `pom.xml` as a Maven project and select JDK 21 as the project SDK.

## Build and run

On Windows PowerShell:

```powershell
.\mvnw.cmd clean package
java -jar target\cowboy-shootout.jar 5
```

On Linux or macOS:

```bash
sh mvnw clean package
java -jar target/cowboy-shootout.jar 5
```

The argument must be a positive whole number. A value of `1` is valid and produces a winner without any shots.

The program prints every shot and the final winner. It creates `shootout-protocol.json` in the current directory and prints the file's SHA-256 checksum. A new run replaces the previous protocol.

## Design

I represent the circle with primitive arrays for health, left neighbors, and right neighbors. This gives constant-time neighbor lookup and removal while avoiding array shifting and per-node object overhead.

The simulation performs one shot at a time because each result determines the next shooter. Randomness is injected through `ShootoutRandom`, which lets the tests use scripted values. A generated seed is printed and stored in the protocol. Using the same cowboy count and seed with `SeededShootoutRandom` reproduces the random sequence in tests or analysis.

Jackson streams each event directly to the JSON file, so the complete history is not held in memory. After the protocol file is closed, it is read as a stream to calculate SHA-256. Since every cowboy starts with 10 HP and each shot removes at least 1 HP, a game has at most `10n - 1` shots. Therefore simulation time and protocol size are `O(n)`; neighbor lookup, damage, and removal are `O(1)`.

## Protocol

The JSON document contains run metadata, the random seed, every shot, and the final result. Each shot includes:

- shooter and target numbers;
- shooter health and direction;
- damage;
- target health after the shot; and
- whether the target was killed.

SHA-256 detects changes to the protocol file. It is an integrity check, not proof of authorship; that would require a protected HMAC key or a digital signature.

## Fairness

Before the starter is selected, the game is fair between named cowboys. The starter is chosen uniformly, all cowboys begin identically, and rotating the labels does not change the rules. Each named cowboy therefore has probability `1 / n` of winning.

The relative positions are not equally favorable once the starter is known. Since every cowboy starts with 10 HP, the first shot always goes right, which disadvantages the immediate right neighbor.

To check the positional effect, I ran the actual simulator from JShell 200,000 times per circle size, using experiment seed `20260905`:

| Cowboys | Most likely relative position | Estimated win rate |
|---:|---|---:|
| 2 | Starter | 65.883% |
| 3 | Immediate left neighbor | 41.608% |
| 4 | Immediate left neighbor | 33.490% |
| 5 | Three positions to the right | 26.963% |
| 10 | Opposite position | 15.288% |

The experiment estimates positional bias; rotational symmetry is the reason the named cowboys are exactly equal before the random starter is chosen.

## Tests

Run all tests with:

```powershell
.\mvnw.cmd clean test
```

The tests cover circle operations, game rules, deterministic randomness, JSON validity, UTF-8 output, known SHA-256 reference values, command-line validation, and an end-to-end run.
