cmake_minimum_required(VERSION 3.10)
project(fireguard_edge_native)

# set output library name
set(LIB_NAME fireguard_native)

add_library(${LIB_NAME} SHARED native-lib.cpp)

# find log lib
find_library(log-lib log)

# include paths for Edge Impulse exports (vision & audio)
# You will put the exported zip contents into these folders
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/vision_model/include)
include_directories(${CMAKE_CURRENT_SOURCE_DIR}/audio_model/include)

# Add static libraries produced by Edge Impulse (update filenames if different)
# For example, the export may include libedgeimpulse.a or libedgeimpulse_vision.a
add_library(edgeimpulse_vision STATIC IMPORTED)
set_target_properties(edgeimpulse_vision PROPERTIES IMPORTED_LOCATION
    ${CMAKE_CURRENT_SOURCE_DIR}/vision_model/lib/libedgeimpulse_vision.a)

add_library(edgeimpulse_audio STATIC IMPORTED)
set_target_properties(edgeimpulse_audio PROPERTIES IMPORTED_LOCATION
    ${CMAKE_CURRENT_SOURCE_DIR}/audio_model/lib/libedgeimpulse_audio.a)

# Link tflite if included in exports (sometimes provided)
# If exported package includes libtensorflowlite.a, add similar IMPORTED target.
# add_library(tflite STATIC IMPORTED)
# set_target_properties(tflite PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/vision_model/lib/libtensorflowlite.a)

# Link libraries
target_link_libraries(${LIB_NAME}
    edgeimpulse_vision
    edgeimpulse_audio
    ${log-lib}
)