import wave
import struct
import math

framerate = 44100
duration = 30 # 30 seconds

def generate_sample(t):
    env = 1.0
    if t < 3.0:
        env = t / 3.0
    elif t > duration - 3.0:
        env = (duration - t) / 3.0
        
    s1 = math.sin(2.0 * math.pi * 261.63 * t)
    s2 = math.sin(2.0 * math.pi * 329.63 * t)
    s3 = math.sin(2.0 * math.pi * 392.00 * t)
    s4 = math.sin(2.0 * math.pi * 130.81 * t)
    
    # slow LFO for some movement
    lfo = math.sin(2.0 * math.pi * 0.2 * t) * 0.2 + 0.8
    
    mixed = (s1 + s2 + s3 + s4 * 0.5) / 3.5
    return int(mixed * env * lfo * 16000)

with wave.open('app/src/main/res/raw/offline_relax.wav', 'w') as wav_file:
    wav_file.setnchannels(1)
    wav_file.setsampwidth(2)
    wav_file.setframerate(framerate)
    
    for i in range(framerate * duration):
        t = i / float(framerate)
        val = generate_sample(t)
        wav_file.writeframesraw(struct.pack('<h', val))
