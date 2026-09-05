import React from 'react';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';

export interface RuntimeMetricItem {
  metricName: string;
  metricValue: number;
  unit?: string;
}

interface MetricsChartProps {
  metrics: RuntimeMetricItem[];
}

export const MetricsChart: React.FC<MetricsChartProps> = ({ metrics }) => {
  const chartData = metrics.map((m) => ({
    name: m.metricName
      .replace('_pct', ' (%)')
      .replace('_mb', ' (MB)')
      .replace('_ms', ' (ms)')
      .replace('_per_req', ' /req'),
    value: m.metricValue,
    unit: m.unit || '',
  }));

  return (
    <div className="w-full h-64">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 20 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#1E293B" />
          <XAxis dataKey="name" stroke="#94A3B8" tick={{ fontSize: 12 }} />
          <YAxis stroke="#94A3B8" tick={{ fontSize: 12 }} />
          <Tooltip
            contentStyle={{ backgroundColor: '#0F172A', borderColor: '#334155', borderRadius: '8px', color: '#F8FAFC' }}
            formatter={(val: number) => [`${val}`, 'Value']}
          />
          <Bar dataKey="value" fill="#10B981" radius={[6, 6, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
};
