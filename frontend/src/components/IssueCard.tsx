import React, { useState } from 'react';
import { AlertTriangle, ChevronDown, ChevronUp, Code, Lightbulb, Scale, Terminal } from 'lucide-react';

export interface StaticIssue {
  id?: number;
  ruleId: string;
  severity: string;
  confidence: string;
  fileName: string;
  lineNumber: number;
  className: string;
  methodName: string;
  callPath: string;
  explanation: string;
  recommendation: string;
  tradeOffs: string;
}

interface IssueCardProps {
  issue: StaticIssue;
}

export const IssueCard: React.FC<IssueCardProps> = ({ issue }) => {
  const [expanded, setExpanded] = useState(false);

  const isHigh = issue.severity === 'HIGH';
  const isMed = issue.severity === 'MEDIUM';

  const badgeBg = isHigh
    ? 'bg-rose-500/10 text-rose-400 border-rose-500/20'
    : isMed
    ? 'bg-amber-500/10 text-amber-400 border-amber-500/20'
    : 'bg-blue-500/10 text-blue-400 border-blue-500/20';

  const fileNameOnly = issue.fileName ? issue.fileName.split(/[/\\]/).pop() : 'UnknownFile.java';

  return (
    <div className="glass-card rounded-xl overflow-hidden border border-slate-800 transition-all hover:border-slate-700">
      <div
        className="p-4 flex items-center justify-between cursor-pointer select-none"
        onClick={() => setExpanded(!expanded)}
      >
        <div className="flex items-center gap-3">
          <div className={`p-2 rounded-lg ${badgeBg} border`}>
            <AlertTriangle className="w-4 h-4" />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <span className="font-mono text-xs font-bold px-2 py-0.5 rounded bg-slate-800 text-slate-200">
                {issue.ruleId}
              </span>
              <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full border uppercase ${badgeBg}`}>
                {issue.severity} SEVERITY
              </span>
            </div>
            <p className="text-sm font-semibold text-slate-100 mt-1 flex items-center gap-2">
              <span>{fileNameOnly}:{issue.lineNumber}</span>
              <span className="text-slate-500">•</span>
              <span className="font-mono text-slate-300 text-xs">{issue.methodName}()</span>
            </p>
          </div>
        </div>

        <button className="p-1.5 rounded-lg bg-slate-800 text-slate-400 hover:text-white transition-colors">
          {expanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
        </button>
      </div>

      {expanded && (
        <div className="p-4 bg-slate-950/60 border-t border-slate-800 space-y-4 text-xs">
          <div>
            <h4 className="font-semibold text-slate-300 flex items-center gap-1.5 mb-1">
              <Terminal className="w-3.5 h-3.5 text-cyan-400" /> Call Path / Context
            </h4>
            <div className="bg-slate-900/80 p-2.5 rounded-lg border border-slate-800 font-mono text-slate-300 overflow-x-auto">
              {issue.callPath || `${issue.className}#${issue.methodName}`}
            </div>
          </div>

          <div>
            <h4 className="font-semibold text-slate-300 flex items-center gap-1.5 mb-1">
              <Code className="w-3.5 h-3.5 text-rose-400" /> Explanation
            </h4>
            <p className="text-slate-400 leading-relaxed bg-slate-900/40 p-2.5 rounded-lg border border-slate-800/60">
              {issue.explanation}
            </p>
          </div>

          <div>
            <h4 className="font-semibold text-slate-300 flex items-center gap-1.5 mb-1">
              <Lightbulb className="w-3.5 h-3.5 text-amber-400" /> Recommended Fix
            </h4>
            <p className="text-emerald-300/90 leading-relaxed bg-emerald-950/20 p-2.5 rounded-lg border border-emerald-500/20">
              {issue.recommendation}
            </p>
          </div>

          {issue.tradeOffs && (
            <div>
              <h4 className="font-semibold text-slate-300 flex items-center gap-1.5 mb-1">
                <Scale className="w-3.5 h-3.5 text-purple-400" /> Performance Trade-Offs
              </h4>
              <p className="text-slate-400 leading-relaxed bg-slate-900/40 p-2.5 rounded-lg border border-slate-800/60">
                {issue.tradeOffs}
              </p>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
