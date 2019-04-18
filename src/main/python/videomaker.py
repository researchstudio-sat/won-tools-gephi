# videomaker assumes each node has a "timestamp" numeric that can be compared
def videomaker(ts_min, ts_max, frames, layout_iterations, frames_before, frames_after, output_prefix, output_format):
  import org.gephi.layout.plugin.noverlap.NoverlapLayoutBuilder
  import os.path
  stopfile = "/tmp/stopfile.txt"

  length = ts_max - ts_min # duration between min and max. these can Integers or Double - just use the same unit everywhere
  interval = length / float(frames) # time interval between each frame
  t = ts_min - interval # set the cursor to the beginning
  setVisible(g.filter(timestamp < t))
  for i in range (0, frames_before):
    runLayout(FruchtermanReingold, iters=layout_iterations)
    exportGraph("%s%s%s" % (output_prefix, i, output_format))
    if os.path.isfile(stopfile): 
        return 
  for i in range(0, frames): # let's start to count from image 0 to image FRAMES
    t += interval 
    setVisible(g.filter(timestamp < t)) # filter using the "timestamp" node attribute. We keep nodes *before* the time cursor
    runLayout(FruchtermanReingold, iters=layout_iterations)
    #runLayout(org.gephi.layout.plugin.noverlap.NoverlapLayoutBuilder, iters=layout_iterations)
    exportGraph("%s%s%s" % (output_prefix, i+frames_before, output_format))
    if os.path.isfile(stopfile):
        return
  for i in range (0, frames_after):
    runLayout(FruchtermanReingold, iters=layout_iterations)
    #runLayout(org.gephi.layout.plugin.noverlap.NoverlapLayoutBuilder, iters=layout_iterations)
    exportGraph("%s%s%s" % (output_prefix, frames_before + frames + i, output_format))
    if os.path.isfile(stopfile):
        return
