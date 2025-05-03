import React from 'react'
import { IoMdDownload } from "react-icons/io";
import { FaFireAlt } from "react-icons/fa";

export default function InformationPart() {
  return (
    <div className="fixed border-[3px] py-[15px] bg-gray-200 border-black bottom-4 left-[130px] shadow-lg p-4 w-[280px] z-50">
    <h1 className="font-bold justify-center flex text-xl mb-1 ">
        SIMULACIJA 12
    </h1>
    <p className="text-md mb-2">Potrebna pomoč: <span className="font-semibold">rešilci</span></p>

    <div className='w-full flex justify-between'>
        <div>
            <div className="text-5xl font-bold text-black mb-2">10<span className="text-base font-medium">min</span></div>
            <p className="text-sm mb-1">Od: <span className="font-medium">Ljubljana 123a</span></p>
            <p className="text-sm mb-3">Do: <span className="font-medium">Grosuplje 23b</span></p>
        </div>
        <div className='bg-gray-200 flex items-center justify-center w-[120px]'>
            <FaFireAlt size={50} color='orange' />           
        </div>
    </div>
    

    <div className="flex justify-between">
      <button className="bg-red-600 hover:bg-red-700 text-white px-3 py-1 rounded">PREKLIČI</button>
      <button className="bg-gray-800 hover:bg-black text-white px-3 py-1 rounded">SHRANI</button>
      <button className="text-gray-600 hover:text-black" title="Prenesi PDF">
        <IoMdDownload size={30} />
      </button>
    </div>
  </div>
  )
}
