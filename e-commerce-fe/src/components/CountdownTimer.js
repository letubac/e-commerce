/**
 * author: LeTuBac
 */
import React from 'react';

const pad = (n) => String(n).padStart(2, '0');

/**
 * Reusable countdown timer display component.
 * Renders HH:MM:SS boxes with an optional label prefix.
 *
 * Props:
 *   hours, minutes, seconds  – current time values
 *   label                    – text before the boxes (e.g. "Kết thúc sau")
 *   boxClassName             – extra classes for each digit box
 *   labelClassName           – extra classes for the label text
 */
export default function CountdownTimer({
  hours = 0,
  minutes = 0,
  seconds = 0,
  label = '',
  boxClassName = 'bg-gray-900 text-white text-base font-bold px-2 py-0.5 rounded min-w-[32px] text-center leading-6',
  labelClassName = 'text-sm opacity-90',
}) {
  const segments = [pad(hours), pad(minutes), pad(seconds)];

  return (
    <div className="flex items-center gap-2">
      {label && <span className={labelClassName}>{label}</span>}
      <div className="flex items-center gap-1">
        {segments.map((val, i) => (
          <React.Fragment key={i}>
            <div className={boxClassName}>{val}</div>
            {i < 2 && <span className="font-bold text-base">:</span>}
          </React.Fragment>
        ))}
      </div>
    </div>
  );
}
