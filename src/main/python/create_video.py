execfile("../../DATA/DEV/workspace/gephi/video/videomaker.py")
videomaker(
    ts_min=1493886171561, # "from" timestamp..
    #ts_max=1559886171561, 
  ts_max=1517219028388, # .."to" timestamp
    frames=4000, # number of images in the video. eg 200 frames for a video at 20 frames per seconds = 10 seconds of video
  layout_iterations=1, #layout iterations between frames
  frames_before=50,
  frames_after=500,
    output_prefix="../../DATA/DEV/workspace/gephi/video/generated_frames/frame_", # path where to write the png. images will be prefixed with "frame_" 
    output_format=".png" # you probably want to leave png here
)