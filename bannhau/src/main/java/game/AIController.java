package game;

import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import java.util.List;

public class AIController {
    private Player player;
    private List<Enemy> enemies;
    private List<EnemyBullet> enemyBullets;
    private List<PowerUp> powerUps;
    private List<Bullet> bullets;
    private BossEnemy boss;
    private double sceneWidth;
    private boolean gameEnded;
    private Pane root;

    public AIController(Player player, List<Enemy> enemies, List<EnemyBullet> enemyBullets,
                        List<PowerUp> powerUps, List<Bullet> bullets, BossEnemy boss,
                        double sceneWidth, boolean gameEnded, Pane root) {
        this.player = player;
        this.enemies = enemies;
        this.enemyBullets = enemyBullets;
        this.powerUps = powerUps;
        this.bullets = bullets;
        this.boss = boss;
        this.sceneWidth = sceneWidth;
        this.gameEnded = gameEnded;
        this.root = root;
    }

    public void updateAI() {
        if (gameEnded) return;

        double px = player.getX();
        double py = player.getY();
        double playerW = player.getFitWidth();
        double playerCenter = px + playerW / 2;
        boolean hasShield = player.hasShield(); // Giả sử hàm này tồn tại

        // === 1. Nếu có khiên, không né đạn, chỉ đứng bắn enemy hoặc boss ===
        if (hasShield) {
            Enemy target = findNearestAliveEnemyAbove(py, playerCenter);
            if (target != null) {
                double enemyCenter = target.getX() + target.getFitWidth() / 2;
                if (enemyCenter < playerCenter - 5) {
                    player.moveLeft();
                } else if (enemyCenter > playerCenter + 5) {
                    player.moveRight(sceneWidth);
                }
                if (Math.abs(enemyCenter - playerCenter) < playerW / 2 + 50) {
                    simulateFire();
                }
                return;
            }

            if (boss != null && boss.isAlive()) {
                double bossCenter = boss.getX() + boss.getFitWidth() / 2;
                if (bossCenter < playerCenter - 5) {
                    player.moveLeft();
                } else if (bossCenter > playerCenter + 5) {
                    player.moveRight(sceneWidth);
                }
                if (Math.abs(bossCenter - playerCenter) < playerW / 2 + 120) {
                    simulateFire();
                }
                return;
            }
        }

        // === 2. Tìm đạn nguy hiểm nhất (chỉ nếu không có khiên) ===
        EnemyBullet dangerBullet = null;
        if (!hasShield) {
            double minDangerDist = Double.MAX_VALUE;
            for (EnemyBullet eb : enemyBullets) {
                double dx = eb.getX() - playerCenter;
                double dy = eb.getY() - py;
                if (dy > -120 && dy < 80 && Math.abs(dx) < playerW * 0.8) {
                    double dist = Math.abs(dy);
                    if (dist < minDangerDist) {
                        minDangerDist = dist;
                        dangerBullet = eb;
                    }
                }
            }
        }

        // === 3. Ưu tiên bắn enemy phía trên ===
        Enemy nearestEnemy = findNearestAliveEnemyAbove(py, playerCenter);
        if (nearestEnemy != null) {
            double enemyCenter = nearestEnemy.getX() + nearestEnemy.getFitWidth() / 2;

            if (dangerBullet != null) {
                if (dangerBullet.getX() < playerCenter) {
                    player.moveRight(sceneWidth);
                } else {
                    player.moveLeft();
                }
            } else {
                if (enemyCenter < playerCenter - 5) {
                    player.moveLeft();
                } else if (enemyCenter > playerCenter + 5) {
                    player.moveRight(sceneWidth);
                }
            }

            if (Math.abs(enemyCenter - playerCenter) < playerW / 2 + 50) {
                simulateFire();
            }
            return;
        }

        // === 4. Bắn boss nếu không còn enemy sống ===
        boolean allEnemiesDead = enemies == null || enemies.stream().allMatch(e -> !e.isAlive());
        if (allEnemiesDead && boss != null && boss.isAlive()) {
            double bossCenter = boss.getX() + boss.getFitWidth() / 2;
            if (dangerBullet != null) {
                if (dangerBullet.getX() < playerCenter) {
                    player.moveRight(sceneWidth);
                } else {
                    player.moveLeft();
                }
            } else {
                if (bossCenter < playerCenter - 5) {
                    player.moveLeft();
                } else if (bossCenter > playerCenter + 5) {
                    player.moveRight(sceneWidth);
                }
            }

            if (Math.abs(bossCenter - playerCenter) < playerW / 2 + 120) {
                simulateFire();
            }
            return;
        }

        // === 5. Cuối cùng mới ăn power-up (nếu an toàn) ===
        if (dangerBullet == null) {
            PowerUp nearestPU = null;
            double minDistPU = Double.MAX_VALUE;
            for (PowerUp pu : powerUps) {
                double dist = Math.abs(pu.getX() - px);
                if (dist < minDistPU && Math.abs(pu.getY() - py) < 150) {
                    minDistPU = dist;
                    nearestPU = pu;
                }
            }
            if (nearestPU != null && minDistPU > 10) {
                if (nearestPU.getX() < px) {
                    player.moveLeft();
                } else {
                    player.moveRight(sceneWidth);
                }
            }
        }
    }

    private Enemy findNearestAliveEnemyAbove(double py, double playerCenter) {
        Enemy nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Enemy enemy : enemies) {
            if (enemy.isAlive() && enemy.getY() < py) {
                double dx = Math.abs((enemy.getX() + enemy.getFitWidth() / 2) - playerCenter);
                if (dx < minDist) {
                    minDist = dx;
                    nearest = enemy;
                }
            }
        }
        return nearest;
    }

    private void simulateFire() {
        System.out.println("AI fired at time: " + System.currentTimeMillis());
        ((SpaceShooter) root.getScene().getWindow().getUserData()).simulateKeyPress(KeyCode.SPACE, root);
    }

    public void setBoss(BossEnemy boss) {
        this.boss = boss;
    }

    public void setGameEnded(boolean gameEnded) {
        this.gameEnded = gameEnded;
    }
}
