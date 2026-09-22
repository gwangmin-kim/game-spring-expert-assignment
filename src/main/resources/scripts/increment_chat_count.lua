local current = tonumber(redis.call('GET', KEYS[1]))
local limit = tonumber(ARGV[1])
if current then
    if current >= limit then
        return 0
    end
    redis.call('INCR', KEYS[1])
else
    redis.call('SET', KEYS[1], 1)
    redis.call('EXPIRE', KEYS[1], ARGV[2])
end
return 1