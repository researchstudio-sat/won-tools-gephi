ffmpeg -r 40 -i ..\..\..\workspace\gephi\video\generated_frames\frame_%d.png -y -s 1000x1000 -qscale:v 5 ..\..\..\workspace\gephi\video\generated_video.mpg