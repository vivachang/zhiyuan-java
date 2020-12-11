local current = redis.call('GET', KEYS[1])
if false == current then
    return nil
end

redis.call('DEL', KEYS[1])

return current