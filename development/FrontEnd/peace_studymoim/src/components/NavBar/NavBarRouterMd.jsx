import { Link } from "react-router-dom";

export default function NavBarRouter() {
  return (
    <>
      {/* TODO: 링크누르면 누른 상태가 보이도록 설정 */}
      <div>
        <Link to="/lecture" className="block py-2 px-4 text-sm hover:bg-[#d0d1ff]">
          <b>강좌</b>
        </Link>
        <Link to="/study" className="block py-2 px-4 text-sm hover:bg-[#d0d1ff]">
          <b>스터디</b>
        </Link>
        <Link to="/community" className="block py-2 px-4 text-sm hover:bg-[#d0d1ff]">
          <b>커뮤니티</b>
        </Link>
      </div>
    </>
  );
}