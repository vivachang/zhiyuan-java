local result = redis.call('SETNX', KEYS[1], ARGV[1])
if 1 == result then
    redis.call('EXPIRE', KEYS[1], ARGV[2])
    return true
end

return false