import React from 'react';
import { ArrowDown, ArrowUp, Minus } from 'lucide-react';

export interface MetricDiff {
  metricName: string;
  valueA: number;
  valueB: number;
  percentChange: number;
}

interface ComparisonTableProps {
  metrics: MetricDiff[];
  scoreA?: number;
  scoreB?: number;
}

export const ComparisonTable: React.FC<ComparisonTableProps> = ({ metrics, scoreA, scoreB }) => {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left border-collapse text-sm">
        <thead>
          <tr className="border-b border-slate-800 text-slate-400 text-xs uppercase tracking-wider bg-slate-900/50">
            <th className="p-3.5">Metric</th>
            <th className="p-3.5">Run A (Before)</th>
            <th className="p-3.5">Run B (After)</th>
            <th className="p-3.5">Net Change</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-800/60">
          {scoreA !== undefined && scoreB !== undefined && (
            <tr className="bg-emerald-950/10 font-semibold">
              <td className="p-3.5 text-slate-200">Overall EcoScore</td>
              <td className="p-3.5 text-slate-300">{scoreA} / 100</td>
              <td className="p-3.5 text-slate-100">{scoreB} / 100</td>
              <td className="p-3.5">
                {scoreB > scoreA ? (
                  <span className="inline-flex items-center gap-1 text-emerald-400 font-bold bg-emerald-500/10 px-2 py-0.5 rounded border border-emerald-500/20">
                    <ArrowUp className="w-3.5 h-3.5" /> +{scoreB - scoreA} pts (Improved)
                  </span>
                ) : scoreB < scoreA ? (
                  <span className="inline-flex items-center gap-1 text-rose-400 font-bold bg-rose-500/10 px-2 py-0.5 rounded border border-rose-500/20">
                    <ArrowDown className="w-3.5 h-3.5" /> {scoreB - scoreA} pts (Regressed)
                  </span>
                ) : (
                  <span className="inline-flex items-center gap-1 text-slate-400">
                    <Minus className="w-3.5 h-3.5" /> No change
                  </span>
                )}
              </td>
            </tr>
          )}

          {metrics.map((m) => {
            const isReduction = m.percentChange < 0;
            const isIncrease = m.percentChange > 0;

            // For runtime, memory, CPU, queries: LOWER is BETTER (green ↓)
            const isPositiveImprovement = isReduction;
            const changeColor = isPositiveImprovement
              ? 'text-emerald-400 bg-emerald-500/10 border-emerald-500/20'
              : isIncrease
              ? 'text-rose-400 bg-rose-500/10 border-rose-500/20'
              : 'text-slate-400 bg-slate-800 border-slate-700';

            return (
              <tr key={m.metricName} className="hover:bg-slate-900/30 transition-colors">
                <td className="p-3.5 font-medium text-slate-300 capitalize">
                  {m.metricName.replace(/_/g, ' ')}
                </td>
                <td className="p-3.5 font-mono text-slate-400">{m.valueA.toFixed(1)}</td>
                <td className="p-3.5 font-mono text-slate-200 font-semibold">{m.valueB.toFixed(1)}</td>
                <td className="p-3.5">
                  <span className={`inline-flex items-center gap-1 font-mono text-xs font-bold px-2 py-0.5 rounded border ${changeColor}`}>
                    {isIncrease && <ArrowUp className="w-3 h-3" />}
                    {isReduction && <ArrowDown className="w-3 h-3" />}
                    {!isIncrease && !isReduction && <Minus className="w-3 h-3" />}
                    {m.percentChange > 0 ? '+' : ''}{m.percentChange.toFixed(1)}%
                  </span>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
};
