execfile("../../DATA/DEV/workspace/won-tools-gephi/src/main/python/videomaker.py")
videomaker(
    ts_min=1493886171561, # "from" timestamp..
    #ts_max=1518108181159, 
    ts_max=1548957040388, # .."to" timestamp
    frames=2500, # number of images in the video. eg 200 frames for a video at 20 frames per seconds = 10 seconds of video
  layout_iterations=2, #layout iterations between frames
  frames_before=50,
  frames_after=200,
    output_prefix="../../DATA/DEV/workspace/gephi/video-all-1902/generated_frames/frame_", # path where to write the png. images will be prefixed with "frame_" 
    output_format=".png" # you probably want to leave png here
)