export default function UpdateProfile() {
  return (
    <div className="w-2/3 mx-auto bg-[#434343] bg-opacity-30 rounded-lg p-8 text-white text-center">
      <div className="flex flex-col items-center">
        <img
          src="/deafult_avatar.png"
          alt="avatar"
          c
          className="w-44 h-44 rounded-full object-cover  mb-6 shadow-md"
        />
        <button className="bg-black bg-opacity-50 px-6 py-2 rounded-md hover:bg-opacity-70 mb-6">
          Change picture
        </button>
      </div>
      <form className="flex flex-col gap-4 items-center">
        <div className="w-1/2 text-left">
          <label className="block mb-1">New username:</label>
          <input
            type="text"
            placeholder="Type username..."
            className="w-full px-4 py-3 bg-black bg-opacity-20 rounded-md text-white  focus:outline-none "
          />
        </div>
        <div className="w-1/2 text-left">
          <label className="block mb-1">New password:</label>
          <input
            type="password"
            placeholder="Type password..."
            className="w-full px-4 py-3 bg-black bg-opacity-20 rounded-md text-white  focus:outline-none "
          />
        </div>
        <div className="w-1/2 text-left">
          <label className="block mb-1">Confirm password:</label>
          <input
            type="password"
            placeholder="Type password..."
            className="w-full px-4 py-3 bg-black bg-opacity-20 rounded-md text-white  focus:outline-none "
          />
        </div>
        <div className="w-1/2 flex justify-end mt-4">
          <button
            type="submit"
            className="bg-black bg-opacity-50 px-6 py-2 rounded-md hover:bg-opacity-70"
          >
            Save
          </button>
        </div>
      </form>
    </div>
  );
}
