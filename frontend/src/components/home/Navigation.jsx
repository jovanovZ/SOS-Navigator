import { Link } from 'react-router-dom';
import { FaUser } from 'react-icons/fa';
import { MdOutlineSos } from 'react-icons/md';

export default function Navigation() {
  const user = JSON.parse(localStorage.getItem('user'));
  const username = user ? user.username : 'Admin';
  const profileImage = user ? user.image : null;

  return (
    <div className='w-full fixed h-[70px] top-0 z-30 flex justify-between items-center px-6 bg-blue-900 border-b-2 border-black'>

      <Link to='/' className='flex items-center gap-2 text-gray-200'>
        <MdOutlineSos className="text-5xl mt-1 text-yellow-400" />
        <span className='font-bold text-2xl'>
          <span className="text-gray-300">- </span>
          <span className="text-red-500">Navigator</span>
        </span>
      </Link>

      <Link to='/profile' className='cursor-pointer flex items-center gap-2'>
        {profileImage ? (
          <img src={profileImage} alt="Profile" className='w-10 h-10 rounded-full border-2 border-gray-200' />
        ) : (
          <FaUser className='text-2xl text-gray-200' />
        )}
        <span className='font-bold text-2xl text-gray-200 uppercase'>{username}</span>
      </Link>

    </div>
  );
}
