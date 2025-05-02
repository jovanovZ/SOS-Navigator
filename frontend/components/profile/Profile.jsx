import React from 'react'

export default function Profile() {
  return (
    <div className="bg-[#434343] bg-opacity-30  w-2/3 h-3/5 mx-auto rounded-b-lg p-8 text-white">
      <div className="h-1/3 flex justify-between ">
        <div className="w-2/3 flex gap-20 ">
          <img
            src="/deafult_avatar.png"
            alt="your avatar"
            className="w-28 h-28 rounded-full object-cover border-2 mb-6 shadow-md"
          />
          <div className=" mt-10">
            <h3 className="text-3xl">Martin</h3>
            <p className="text-xl">martin.kobal1@gmail.com</p>
          </div>
        </div>
        <div className="flex items-center ">
          <button className="bg-[#000000] bg-opacity-50 py-2 px-8 rounded-md hover:bg-[#1e1e1e]">
            Edit
          </button>
        </div>
      </div>
      <hr className="h-0.5 border-0 bg-gradient-to-r from-[#666666] via-[#393838] to-[#666666]" />
      <div className="h-2/3 mt-6">
        <h3 className="text-3xl ">Zgodovina</h3>
        <p className="text-xl mt-1">Podrobnosti simulacij</p>
        <table className="w-full text-left mt-5 overflow-hidden rounded-t-lg ">
          <thead className="bg-[#000000] bg-opacity-50 ">
            <tr>
              <th className="px-4 py-2">📅 Datum</th>
              <th className="px-4 py-2">📍 Od</th>
              <th className="px-4 py-2">📌 Do</th>
              <th className="px-4 py-2">🚑 Potrebne postaje</th>
              <th className="px-4 py-2">🕒 Potreben čas</th>
            </tr>
          </thead>
          <tbody className="bg-[#989898] bg-opacity-30 ">
            <tr className="hover:bg-[#1e1e1e]">
              <th className="px-4 py-2">1.05.2025</th>
              <th className="px-4 py-2">Ljubljana 123a</th>
              <th className="px-4 py-2">Grosuplje 12b</th>
              <th className="px-4 py-2">Policija, rešilci, gasilci</th>
              <th className="px-4 py-2">1h 15 minut</th>
            </tr>
            <tr className="hover:bg-[#1e1e1e]">
              <th className="px-4 py-2">1.05.2025</th>
              <th className="px-4 py-2">Ljubljana 123a</th>
              <th className="px-4 py-2">Grosuplje 12b</th>
              <th className="px-4 py-2">Policija, rešilci, gasilci</th>
              <th className="px-4 py-2">1h 15 minut</th>
            </tr>
            <tr className="hover:bg-[#1e1e1e]">
              <th className="px-4 py-2">1.05.2025</th>
              <th className="px-4 py-2">Ljubljana 123a</th>
              <th className="px-4 py-2">Grosuplje 12b</th>
              <th className="px-4 py-2">Policija, rešilci, gasilci</th>
              <th className="px-4 py-2">1h 15 minut</th>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
}