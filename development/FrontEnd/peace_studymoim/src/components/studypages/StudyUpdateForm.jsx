import { useEffect } from "react";
import { useRef, useState } from "react";
import ReactQuill from "react-quill";
import "react-quill/dist/quill.snow.css";
import DeleteModal from "../overall/DeleteModal";
import useFetch from "../../hooks/useFetch";
import userInfo from "../../zustand/store";
import Select from "react-select";

export default function StudyMakeForm(props) {
  const [showModal, setShowModal] = useState(false);
  const [selectedOptions, setSelectedOptions] = useState([]); 
  const API_SERVER = import.meta.env.VITE_APP_API_SERVER;
  const search = useFetch(`http://${API_SERVER}/api/v1/course/`);

  function closeModalHandler() {
    setShowModal(false);
  }

  const optionList = search.map((course) =>
    Object.assign({ value: course.course_id, label: course.title })
  );

  function handleSelect(data) {
    setSelectedOptions(data);
  }

  const { info } = userInfo();

  // if (!info) {
  //   alert("로그인이 필요합니다.");
  //   navigate("/login");
  // }

  const [image, setImage] = useState(null);
  const [preview, setPreview] = useState(null);

  const titleInputRef = useRef("");
  const descriptionRef = useRef("");
  const startDateRef = useRef("");
  const recruitMembersRef = useRef();
  const recruitMethodRef = useRef();
  const selectCourseRef = useRef([]); 

  useEffect(() => {
    if (image) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setPreview(reader.result);
      };
      reader.readAsDataURL(image);
    } else {
      setPreview(null);
    }
  }, [image]);

  function submitHandler(event) {
    event.preventDefault();

    const enteredRecruitMembers = recruitMembersRef.current.value;
    const enteredStartDate = startDateRef.current.value;
    const enteredRecruitMethod = recruitMethodRef.current.value;
    const enteredTitleInput = titleInputRef.current.value;
    const enteredDescription = descriptionRef.current.value;
    const enteredSelectOptions = selectedOptions.map((course) => {
      return course.value
    })
 
    const studyRecruitData = {
      title: enteredTitleInput,
      content: enteredDescription,
      startTime: enteredStartDate,
      saveName: image,
      userLimit: enteredRecruitMembers,
      courseIdList: enteredSelectOptions,
      leadUserId: info.userId,
      public: enteredRecruitMethod,
    };
    console.log(studyRecruitData) 
    props.onAddMeetup(studyRecruitData);
  }

  return (
    <div className="max-w-6xl mx-auto px-4 bg-white my-[100px]">
      <form className="w-full gap-4 py-5" onSubmit={submitHandler}>
        <div className="flex flex-col justify-start items-center w-full gap-2.5 py-[5px]">
          <p className="text-4xl text-left font-bold">
            스터디 기본정보를 입력해주세요
          </p>
          {/* 보라색 선 */}
          <svg
            height={9}
            viewBox="0 0 1352 9"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            className="w-full mt-3 flex-grow-0 flex-shrink-0"
            preserveAspectRatio="none"
          >
            <line
              x1="0.993338"
              y1="7.50001"
              x2="1351.99"
              y2="7.50001"
              stroke="#7B61FF"
              strokeWidth={3}
            />
          </svg>

          <div className="grid grid-cols-2 p-2.5">
            {/* 모집인원 */}
            <div className="flex flex-col justify-start items-start self-stretch flex-grow relative gap-2.5 p-2.5">
              <p className="text-[32px] text-left">모집인원(*)</p>
              <select
                id="recruitMembers"
                ref={recruitMembersRef}
                required
                className="w-full h-[90px] relative rounded border-2 border-[#b1b2ff]"
              >
                <option></option>
                <option value={1}>1명</option>
                <option value={2}>2명</option>
                <option value={3}>3명</option>
                <option value={4}>4명</option>
                <option value={5}>5명</option>
                <option value={6}>6명</option>
              </select>
            </div>

            {/* 시작 예정일 */}
            <div className="flex flex-col justify-start items-start self-stretch flex-grow relative gap-2.5 p-2.5">
              <p className="text-[32px] text-left">시작 예정일(*)</p>
              <input
                required
                id="startDate"
                type="date"
                min="2023-01-01"
                max="2024-12-31"
                ref={startDateRef}
                className="w-full h-[90px] relative rounded border-2 border-[#b1b2ff]"
              />
            </div>

            {/* 인원 모집 방법 */}
            <div className="flex flex-col justify-start items-start self-stretch flex-grow relative gap-2.5 p-2.5">
              <p className="text-[32px] text-left">인원 모집 방법(*)</p>
              <select
                id="recruitMethod"
                ref={recruitMethodRef}
                required
                className="w-full h-[90px] relative rounded border-2 border-[#b1b2ff]"
              >
                <option></option>
                <option value={true}>공개</option>
                <option value={false}>수락</option>
              </select>
            </div>

            {/* TODO: 강좌는 나중에 !! 다중 선택으로 해야 돼!!  */}
            <div className="flex flex-col justify-start items-start self-stretch flex-grow relative gap-2.5 p-2.5">
              <p className="text-[32px] text-left">강좌 선택(*)</p>
              <div className="w-full">
                <Select
                  options={optionList}
                  placeholder="강좌를 검색하세요."
                  value={selectedOptions}
                  onChange={handleSelect}
                  isSearchable={true}
                  isMulti
                  required
                  ref={selectCourseRef} 
                />
              </div>
            </div>
            
            {/* finished 여부  */}
            <div className="flex flex-col justify-start items-start self-stretch flex-grow relative gap-2.5 p-2.5">
              <p className="text-[32px] text-left">모집마감 여부(*)</p>
              <select
                id="recruitMethod"
                ref={recruitMethodRef}
                required
                className="w-full h-[90px] relative rounded border-2 border-[#b1b2ff]"
              >
                <option></option>
                <option value={true}>마감했어요</option>
                <option value={false}>아직모집중ㅋ</option>
              </select>
            </div>

            {/* close 여부 */}
            <div className="flex flex-col justify-start items-start self-stretch flex-grow relative gap-2.5 p-2.5">
              <p className="text-[32px] text-left">스터디 종료(*)</p>
              <select
                id="recruitMethod"
                ref={recruitMethodRef}
                required
                className="w-full h-[90px] relative rounded border-2 border-[#b1b2ff]"
              >
                <option></option>
                <option value={true}>종료</option>
                <option value={false}>아직 진행중</option>
              </select>
            </div>

            {/* 사진 */}
            <div className="flex justify-start items-start relative gap-2.5 p-2.5 w-full">
              {preview ? (
                <img
                  src={preview}
                  required
                  className="flex-grow-0 flex-shrink-0 w-6/12 object-cover rounded-full"
                />
              ) : (
                <img
                  src={"/logo.png"}
                  className="flex-grow-0 flex-shrink-0 w-6/12 object-cover rounded-full"
                />
              )}

              <div className="flex flex-col justify-start items-start flex-grow-0 flex-shrink-0 gap-[21px] px-4">
                <input
                 className="block file:mr-4 file:py-2 file:px-4
                 file:rounded-full file:border-0
                 file:text-sm file:font-semibold
                 file:bg-violet-100 file:text-violet-700
                 hover:file:bg-violet-300"
                  id="picture"
                  type="file"
                  accept="image/*"
                  onChange={(event) => {
                    const file = event.target.files[0];
                    if (file && file.type.substring(0, 5) === "image") {
                      setImage(file);
                    } else {
                      setImage(null);
                    }
                  }}
                />
              </div>
            </div>
          </div>
        </div>

        <div className="flex flex-col justify-start items-center flex-grow-0 flex-shrink-0 w-full gap-2.5 py-[5px]">
          <p className="text-4xl text-left font-bold">
            스터디에 대해 설명해주세요
          </p>
          <svg
            height={9}
            viewBox="0 0 1352 9"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            className="w-full mt-3 flex-grow-0 flex-shrink-0"
            preserveAspectRatio="none"
          >
            <line
              x1="0.993338"
              y1="7.50001"
              x2="1351.99"
              y2="7.50001"
              stroke="#7B61FF"
              strokeWidth={3}
            />
          </svg>
          {/* 제목 */}
          <div className="flex flex-col w-full justify-start items-end flex-grow-0 flex-shrink-0 gap-[34px]">
            <input
              type="text"
              ref={titleInputRef}
              id="title"
              required
              className="w-full mt-3 h-[50px] justify-center border"
              placeholder="제목을 입력해주세요"
              min={5}
              max={30}
            />
            {/* 설명 */} 
            {/* TODO : CK editor로 바꿔야함!  */}
            <ReactQuill
              id="description"
              ref={descriptionRef}
              required
              placeholder="스터디에 대해 소개해주세요(선택)&#13;첫 회의 날짜: 1/17 8시&#13;주 3회 월수금 예정입니다."
              className="w-full h-[400px] justify-center mb-5"
            />

            <div className="flex justify-center items-center flex-grow-0 flex-shrink-0 relative gap-[15px]">
              <div
                className="btn flex-grow-0 flex-shrink-0 w-[107px] h-[60px] relative rounded-[10px] bg-[#fc7a6f] text-center items-center text-4xl text-white p-2"
                onClick={() => setShowModal(true)}
              >
                취소
              </div>
              <button className="flex-grow-0 flex-shrink-0 w-[131px] h-[60px] relative rounded-[10px] bg-[#a259ff]  text-white text-4xl">
                수정하기
              </button>

              {showModal ? (
                <DeleteModal
                  onCancel={closeModalHandler}
                  onConfirm={closeModalHandler}
                />
              ) : null}
            </div>
          </div>
        </div>
      </form>
    </div>
  );
}