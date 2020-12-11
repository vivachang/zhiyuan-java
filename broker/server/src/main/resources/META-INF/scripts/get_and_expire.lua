local current = redis.call('GET', KEYS[1])
if false == current then
    return nil
end

redis.call('EXPIRE', KEYS[1], ARGV[1])

return current