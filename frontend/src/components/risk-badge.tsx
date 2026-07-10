import { Badge } from "@/components/ui/badge";
import type { RiskLevel } from "@/src/lib/mock-data";
import { cn } from "@/lib/utils";

const LABELS: Record<RiskLevel, string> = {
  low: "Low",
  medium: "Med",
  high: "High",
};

export function RiskBadge({
  level,
  score,
  className,
}: {
  level: RiskLevel;
  score: number;
  className?: string;
}) {
  return (
    <Badge
      variant={level === "high" ? "destructive" : level === "medium" ? "secondary" : "outline"}
      className={cn("h-5 px-1.5 text-xs tabular-nums", className)}
      title={`Risk score ${score} of 100`}
    >
      {LABELS[level]} {score}
    </Badge>
  );
}
