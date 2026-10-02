import React, { useEffect, useRef } from "react";
import { NODES_BACKGROUND_CONFIG } from "../config/constants";
import styles from "./NodesBackground.module.css";

interface NodeParticle {
  x: number;
  y: number;
  dx: number;
  dy: number;
  radius: number;
}

export const NodesBackground: React.FC = () => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext("2d");
    if (!ctx) return;

    let animationFrameId: number;
    let width = (canvas.width = window.innerWidth);
    let height = (canvas.height = window.innerHeight);

    const NODE_COUNT = NODES_BACKGROUND_CONFIG.NODE_COUNT;
    const MAX_DISTANCE = NODES_BACKGROUND_CONFIG.MAX_DISTANCE;
    const NODE_COLOR_BASE = NODES_BACKGROUND_CONFIG.NODE_COLOR_BASE;

    const particles: NodeParticle[] = Array.from({ length: NODE_COUNT }, () => {
      const radius = 4 + Math.random() * 8;
      return {
        x: radius + Math.random() * Math.max(width - radius * 2, 10),
        y: radius + Math.random() * Math.max(height - radius * 2, 10),
        dx: (Math.random() - 0.5) * 0.1,
        dy: (Math.random() - 0.5) * 0.1,
        radius,
      };
    });

    const handleResize = () => {
      if (!canvas) return;
      width = canvas.width = window.innerWidth;
      height = canvas.height = window.innerHeight;

      particles.forEach((p) => {
        if (p.x > width - p.radius) p.x = Math.max(p.radius, width - p.radius);
        if (p.x < p.radius) p.x = p.radius;
        if (p.y > height - p.radius) p.y = Math.max(p.radius, height - p.radius);
        if (p.y < p.radius) p.y = p.radius;
      });
    };

    window.addEventListener("resize", handleResize);

    const animate = () => {
      ctx.clearRect(0, 0, width, height);

      for (let i = 0; i < particles.length; i++) {
        const p = particles[i];

        p.x += p.dx;
        p.y += p.dy;

        if (p.x - p.radius <= 0) {
          p.x = p.radius;
          p.dx = Math.abs(p.dx);
        } else if (p.x + p.radius >= width) {
          p.x = width - p.radius;
          p.dx = -Math.abs(p.dx);
        }

        if (p.y - p.radius <= 0) {
          p.y = p.radius;
          p.dy = Math.abs(p.dy);
        } else if (p.y + p.radius >= height) {
          p.y = height - p.radius;
          p.dy = -Math.abs(p.dy);
        }
      }

      for (let i = 0; i < particles.length; i++) {
        for (let j = i + 1; j < particles.length; j++) {
          const p1 = particles[i];
          const p2 = particles[j];

          const diffX = p1.x - p2.x;
          const diffY = p1.y - p2.y;
          const distance = Math.hypot(diffX, diffY);

          if (distance < MAX_DISTANCE) {
            const lineOpacity = (1 - distance / MAX_DISTANCE) * 0.4;

            ctx.beginPath();
            ctx.moveTo(p1.x, p1.y);
            ctx.lineTo(p2.x, p2.y);
            ctx.strokeStyle = `rgba(${NODE_COLOR_BASE}, ${lineOpacity})`;
            ctx.lineWidth = 1.2;
            ctx.stroke();
          }
        }
      }

      // 3. Dibujar nodos (círculos con 60% de opacidad)
      for (let i = 0; i < particles.length; i++) {
        const p = particles[i];

        ctx.beginPath();
        ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
        ctx.fillStyle = `rgba(${NODE_COLOR_BASE}, 0.6)`;
        ctx.fill();
      }

      animationFrameId = requestAnimationFrame(animate);
    };

    animate();

    return () => {
      cancelAnimationFrame(animationFrameId);
      window.removeEventListener("resize", handleResize);
    };
  }, []);

  return (
    <canvas
      ref={canvasRef}
      className={styles.canvas}
      aria-hidden="true"
    />
  );
};

export default NodesBackground;
