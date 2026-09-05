import React from 'react';

interface EcoScoreGaugeProps {
  score: number;
  size?: number;
  strokeWidth?: number;
  showLabel?: boolean;
}

export const EcoScoreGauge: React.FC<EcoScoreGaugeProps> = ({
  score,
  size = 180,
  strokeWidth = 14,
  showLabel = true,
}) => {
  const normalizedScore = Math.max(0, Math.min(100, score));
  const radius = (size - strokeWidth) / 2;
  const circumference = 2 * Math.PI * radius;
  const strokeDashoffset = circumference - (normalizedScore / 100) * circumference;

  let colorClass = 'text-emerald-500';
  let strokeColor = '#10B981';
  let glowClass = 'glow-emerald';
  let ratingText = 'OPTIMAL';

  if (normalizedScore < 40) {
    colorClass = 'text-rose-500';
    strokeColor = '#EF4444';
    glowClass = 'glow-red';
    ratingText = 'CRITICAL';
  } else if (normalizedScore < 60) {
    colorClass = 'text-amber-500';
    strokeColor = '#F59E0B';
    glowClass = 'glow-amber';
    ratingText = 'MODERATE';
  }

  return (
    <div className="flex flex-col items-center justify-center p-4">
      <div className={`relative flex items-center justify-center rounded-full ${glowClass}`}>
        <svg width={size} height={size} className="transform -rotate-90">
          <circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke="currentColor"
            strokeWidth={strokeWidth}
            className="text-slate-800"
            fill="transparent"
          />
          <circle
            cx={size / 2}
            cy={size / 2}
            r={radius}
            stroke={strokeColor}
            strokeWidth={strokeWidth}
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
            strokeLinecap="round"
            fill="transparent"
            className="transition-all duration-1000 ease-out"
          />
        </svg>
        <div className="absolute flex flex-col items-center justify-center text-center">
          <span className={`text-4xl font-black tracking-tight ${colorClass}`}>
            {normalizedScore}
          </span>
          <span className="text-[10px] font-semibold tracking-widest text-slate-400 uppercase mt-0.5">
            / 100
          </span>
        </div>
      </div>
      {showLabel && (
        <div className="mt-3 text-center">
          <span className={`text-xs font-extrabold tracking-widest px-2.5 py-1 rounded-full uppercase border ${
            normalizedScore >= 60
              ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
              : normalizedScore >= 40
              ? 'bg-amber-500/10 text-amber-400 border-amber-500/20'
              : 'bg-rose-500/10 text-rose-400 border-rose-500/20'
          }`}>
            {ratingText} ECO SCORE
          </span>
        </div>
      )}
    </div>
  );
};
