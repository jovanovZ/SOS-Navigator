export default function Loading({text}) {
  return (
    <div className="fixed inset-0 z-9999 flex items-center justify-center bg-white/70 backdrop-blur-sm">
      <div className="h-12 w-12 border-4 border-blue-500 border-t-transparent rounded-full animate-spin" />
      {text && <span className="ml-4 text-blue-500">{text}...</span>}
    </div>
  );
}
