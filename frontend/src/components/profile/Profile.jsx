import React from 'react'
import Navigation from '../home/Navigation';
import { Link } from 'react-router-dom'


const historyExample = [
  { id: 1, date: '1.05.2021', from: 'Ljubljana 123a', to: 'Grosuplje 12b', stations: 'Policija, rešilci', time: '45 minut' },
  { id: 2, date: '1.12.2025', from: 'Ljubljana 123a', to: 'Grosuplje 12b', stations: 'Policija, rešilci, gasilci', time: '1h 15 minut' },
  { id: 3, date: '1.12.2022', from: 'Ljubljana 123a', to: 'Grosuplje 12b', stations: 'Gasilci', time: '15 minut' },
  { id: 4, date: '1.05.2025', from: 'Ljubljana 123a', to: 'Grosuplje 12b', stations: 'Policija, gasilci', time: '2h 35 minut' },
  { id: 5, date: '1.06.2024', from: 'Ljubljana 123a', to: 'Grosuplje 12b', stations: 'Policija, gasilci', time: '25 minut' },
  { id: 6, date: '1.06.2025', from: 'Ljubljana 123a', to: 'Grosuplje 12b', stations: 'Policija, rešilci', time: '1h ' },
]
export default function Profile() {
  return (
    <>
    <Navigation/>
    <div className="rounded-b-lg p-8 text-black mt-14 text-large">
      <div className="h-1/3 flex justify-between ">
        <div className="w-2/3 flex gap-20 ">
          <img
            src="/deafult_avatar.png"
            alt="your avatar"
            className="w-28 h-28 rounded-full object-cover border-2 mb-6 shadow-md"
          />
          <div className=" mt-10">
            <h3 className="text-3xl mb-1 ">Martin</h3>
            <p className="text-xl">martin.kobal1@gmail.com</p>
          </div>
        </div>
        <div className="flex items-center ">
          <Link to='/update' className='cursor-pointer'>
            <button className="bg-[#000000] bg-opacity-70 py-2 px-8 rounded-md hover:bg-[#1e1e1e] text-white">
              Edit
            </button>
          </Link>
        </div>
      </div>
      <hr className="h-0.5 border-0 bg-gradient-to-r from-[#666666] via-[#393838] to-[#666666]" />
      <div className="h-2/3 mt-6">
        <h3 className="text-3xl ">Zgodovina</h3>
        <p className="text-xl mt-1">Podrobnosti simulacij</p>
        <table className="w-full text-left mt-5 overflow-hidden rounded-t-lg text-white  table-auto">
          <thead className="bg-[#000000] bg-opacity-70">
            <tr>
              <th className="px-4 py-2">📅 Datum</th>
              <th className="px-4 py-2">📍 Od</th>
              <th className="px-4 py-2">📌 Do</th>
              <th className="px-4 py-2">🚑 Potrebne postaje</th>
              <th className="px-4 py-2">🕒 Potreben čas</th>
            </tr>
          </thead>
          <tbody className="bg-[#989898]  ">
            {historyExample.map((item) => {
              return (
                <tr key={item.id} className="hover:bg-[#1e1e1e] hover:bg-opacity-80">
                  <th className="px-4 py-2">{item.date}</th>
                  <th className="px-4 py-2">{item.from}</th>
                  <th className="px-4 py-2">{item.to}</th>
                  <th className="px-4 py-2">{item.stations}</th>
                  <th className="px-4 py-2">{item.time}</th>
                </tr>
              );})
            }
          </tbody>
        </table>
      </div>
    </div>
    </>
  );
}