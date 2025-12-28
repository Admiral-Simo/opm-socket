"use client";

import React from "react";

type Props = {
  open: boolean;
  title?: string;
  message: string;
  onClose: () => void;
};

export default function Modal({ open, title = "Error", message, onClose }: Props) {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div
        className="absolute inset-0 bg-black/50"
        onClick={onClose}
        aria-hidden
      />

      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
        className="relative z-10 w-full max-w-md rounded-lg bg-white p-6 shadow-lg dark:bg-gray-800"
      >
        <h3 id="modal-title" className="mb-2 text-lg font-bold text-gray-900 dark:text-gray-100">
          {title}
        </h3>
        <div className="mb-4 text-sm text-gray-700 dark:text-gray-200">{message}</div>
        <div className="flex justify-end">
          <button
            onClick={onClose}
            className="rounded bg-blue-600 px-4 py-2 text-white hover:bg-blue-500"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
}
