import { Link } from 'react-router-dom';
import { FaUser } from 'react-icons/fa';
import { MdOutlineSos } from 'react-icons/md';

export default function Navigation() {
  return (
    <div className='w-full fixed h-[70px] top-0 z-30 flex justify-between items-center px-6 bg-blue-900 border-b-2 border-black'>

      {/* Leva stran z logotipom */}
      <Link to='/' className='flex items-center gap-2 text-gray-200'>
        <MdOutlineSos className="text-5xl mt-1 text-yellow-400" />
        <span className='font-bold text-2xl'>
          <span className="text-gray-300">- </span>
          <span className="text-red-500">Navigator</span>
        </span>
      </Link>

      {/* Desna stran z uporabnikom */}
      <Link to='/profile' className='cursor-pointer flex items-center gap-2'>
        <FaUser className='text-2xl text-gray-200' />
        <span className='font-bold text-2xl text-gray-200'>MARTIN</span>
      </Link>

    </div>
  );
}
